package pe.edu.pe.PharmaBackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.pe.PharmaBackend.auditoria.EventoAuditoria;
import pe.edu.pe.PharmaBackend.dto.DetalleVentaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.pe.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.pe.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.pe.PharmaBackend.service.service.ProductoService;
import pe.edu.pe.PharmaBackend.service.service.VentaService;

@SpringBootTest(properties = {"spring.data.redis.database=15", "logging.level.org.hibernate.SQL=OFF"})
class AuditoriaIntegracionTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductoService productos;
    @Autowired VentaService ventas;
    @Autowired TransactionTemplate transacciones;
    @Autowired ApplicationEventPublisher eventos;
    private Long producto;
    private Long sinStock;
    private Long cliente;
    private Long venta;
    private String requestId;

    @BeforeEach
    void preparar() {
        requestId = "s14-test-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("vendedor", null, List.of()));
        Long categoria = jdbc.queryForObject("select min(id) from categorias where estado = true", Long.class);
        cliente = jdbc.queryForObject("select min(id) from clientes where estado = true", Long.class);
        producto = productos.create(new ProductoRequestDTO(requestId, "Prueba aislada", new BigDecimal("4.50"), 10, categoria, true)).getId();
        sinStock = productos.create(new ProductoRequestDTO(requestId + "-vacio", "Prueba aislada", new BigDecimal("1.00"), 0, categoria, true)).getId();
    }

    @AfterEach
    void limpiarSoloRegistrosPropios() {
        if (venta != null) {
            jdbc.update("delete from detalle_ventas where venta_id = ?", venta);
            jdbc.update("delete from ventas where id = ?", venta);
        }
        jdbc.update("delete from auditoria_eventos where request_id = ?", requestId);
        if (producto != null) jdbc.update("delete from producto where id = ?", producto);
        if (sinStock != null) jdbc.update("delete from producto where id = ?", sinStock);
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void ventaConfirmadaGuardaUsuarioStockYEventoConRequestId() {
        var respuesta = ventas.registrar(new VentaRequestDTO(cliente,
                List.of(new DetalleVentaRequestDTO(producto, 2))));
        venta = respuesta.getId();
        assertThat(respuesta.getCreadoPor()).isEqualTo("vendedor");
        assertThat(jdbc.queryForObject("select stock from producto where id = ?", Integer.class, producto)).isEqualTo(8);
        var evento = jdbc.queryForMap("select usuario, accion, entidad_id, request_id from auditoria_eventos where request_id = ?", requestId);
        assertThat(evento.get("USUARIO")).isEqualTo("vendedor");
        assertThat(evento.get("ACCION")).isEqualTo("VENTA_REGISTRADA");
        assertThat(evento.get("REQUEST_ID")).isEqualTo(requestId);
        assertThat(((Number) evento.get("ENTIDAD_ID")).longValue()).isEqualTo(venta);
    }

    @Test
    void segundaLineaSinStockReviertePrimeraLineaYNoDejaEvento() {
        assertThatThrownBy(() -> ventas.registrar(new VentaRequestDTO(cliente, List.of(
                new DetalleVentaRequestDTO(producto, 2), new DetalleVentaRequestDTO(sinStock, 1)))))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(jdbc.queryForObject("select stock from producto where id = ?", Integer.class, producto)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select count(*) from auditoria_eventos where request_id = ?", Integer.class, requestId)).isZero();
    }

    @Test
    void eventoPublicadoEnTransaccionRevertidaNoSeRegistra() {
        transacciones.executeWithoutResult(status -> {
            eventos.publishEvent(EventoAuditoria.de("VENTA_REGISTRADA", "Venta", -1L, "rollback de prueba"));
            status.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("select count(*) from auditoria_eventos where request_id = ?", Integer.class, requestId)).isZero();
    }
}
