package pe.edu.pe.PharmaBackend.service.impl;

import org.junit.jupiter.api.Test;
import pe.edu.pe.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ClienteRequestDTO;
import pe.edu.pe.PharmaBackend.entity.Categoria;
import pe.edu.pe.PharmaBackend.entity.Cliente;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.repository.*;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import pe.edu.pe.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.DetalleVentaRequestDTO;
import pe.edu.pe.PharmaBackend.entity.Producto;
import pe.edu.pe.PharmaBackend.entity.Venta;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BajaLogicaTest {
    @Test void productoRepetidoSeRechazaAntesDeDescontarStock() {
        var servicio = new VentaServiceImpl(ventas, clientes, productos);
        var request = new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(2L, 1), new DetalleVentaRequestDTO(2L, 2)));
        var error = assertThrows(ReglaNegocioException.class, () -> servicio.registrar(request));
        assertTrue(error.getMessage().contains("repetido"));
        verifyNoInteractions(clientes, productos, ventas);
    }
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final CategoriaServiceImpl categoriaService = new CategoriaServiceImpl(categorias, productos);
    private final ClienteServiceImpl clienteService = new ClienteServiceImpl(clientes, ventas);

    private Categoria categoria() {
        Categoria c = new Categoria(); c.setId(1L); c.setEstado(true);
        when(categorias.findById(1L)).thenReturn(Optional.of(c));
        return c;
    }
    private Cliente cliente() {
        Cliente c = new Cliente(); c.setId(1L); c.setEstado(true);
        when(clientes.findById(1L)).thenReturn(Optional.of(c));
        return c;
    }
    @Test void categoriaSinProductosSeConservaInactiva() {
        Categoria c = categoria(); categoriaService.delete(1L);
        assertFalse(c.getEstado()); assertNotNull(c.getFechaModificacion());
        verify(categorias).save(c); verify(categorias, never()).delete(any(Categoria.class));
    }
    @Test void categoriaConProductosNoSeModifica() {
        Categoria c = categoria(); when(productos.existsByCategoriaId(1L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> categoriaService.delete(1L));
        assertTrue(c.getEstado()); verify(categorias, never()).save(any());
    }
    @Test void clienteSinVentasSeConservaInactivo() {
        Cliente c = cliente(); clienteService.delete(1L);
        assertFalse(c.getEstado()); verify(clientes).save(c);
        verify(clientes, never()).delete(any(Cliente.class));
    }
    @Test void clienteConVentasNoSeModifica() {
        Cliente c = cliente(); when(ventas.existsByClienteId(1L)).thenReturn(true);
        var error = assertThrows(ReglaNegocioException.class, () -> clienteService.delete(1L));
        assertTrue(error.getMessage().contains("ventas asociadas"));
        assertTrue(c.getEstado()); verify(clientes, never()).save(any());
    }
    @Test void editarEstadoRespetaProductosAsociados() {
        Categoria c = categoria(); when(productos.existsByCategoriaId(1L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> categoriaService.update(1L,
                new CategoriaRequestDTO("Vitaminas", "", false)));
        assertTrue(c.getEstado()); verify(categorias, never()).save(any());
    }
    @Test void editarEstadoRespetaVentasAsociadas() {
        Cliente c = cliente(); when(ventas.existsByClienteId(1L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> clienteService.update(1L,
                new ClienteRequestDTO("12345678", "Ana", "Perez", "ana@example.com", null, null, false)));
        assertTrue(c.getEstado()); verify(clientes, never()).save(any());
    }
    @Test void noSeRepiteLaBajaDeUnInactivo() {
        categoria().setEstado(false); cliente().setEstado(false);
        assertThrows(ReglaNegocioException.class, () -> categoriaService.delete(1L));
        assertThrows(ReglaNegocioException.class, () -> clienteService.delete(1L));
        verify(categorias, never()).save(any()); verify(clientes, never()).save(any());
    }
    @Test void ventaRechazaStockQueOtraCajaYaDesconto() {
        cliente();
        Producto p = new Producto(); p.setId(2L); p.setEstado(true); p.setStock(5);
        when(productos.findById(2L)).thenReturn(Optional.of(p));
        when(productos.descontarStock(2L, 3)).thenReturn(0);
        var servicio = new VentaServiceImpl(ventas, clientes, productos);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(
                new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(2L, 3)))));
        verify(ventas, never()).save(any());
    }
    @Test void ventaUsaDescuentoCondicionalYCalculaTotalEnServidor() {
        cliente();
        Producto p = new Producto(); p.setId(2L); p.setNombre("Prueba");
        p.setEstado(true); p.setStock(5); p.setPrecio(new BigDecimal("2.50"));
        when(productos.findById(2L)).thenReturn(Optional.of(p));
        when(productos.descontarStock(2L, 3)).thenReturn(1);
        when(ventas.save(any())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0); v.setId(1L); return v;
        });
        new VentaServiceImpl(ventas, clientes, productos).registrar(
                new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(2L, 3))));
        verify(productos).descontarStock(2L, 3);
        verify(ventas).save(argThat(v -> new BigDecimal("7.50").equals(v.getTotal())));
        // El producto administrado no debe sobrescribir el UPDATE con stock obsoleto.
        assertEquals(5, p.getStock());
    }
}
