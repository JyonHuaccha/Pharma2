package pe.edu.pe.PharmaBackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.pe.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.ClienteRequestDTO;
import pe.edu.pe.PharmaBackend.service.service.CategoriaService;
import pe.edu.pe.PharmaBackend.service.service.ClienteService;

@SpringBootTest(properties = {"spring.data.redis.database=15", "logging.level.org.hibernate.SQL=OFF"})
class AuditoriaNuevosEventosTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired ClienteService clientes;
    @Autowired CategoriaService categorias;
    @Autowired TransactionTemplate transacciones;
    private String requestId;
    private Long cliente;
    private Long categoria;

    @BeforeEach
    void preparar() {
        requestId = "s14a-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of()));
    }

    @AfterEach
    void limpiar() {
        jdbc.update("delete from auditoria_eventos where request_id = ?", requestId);
        if (cliente != null) jdbc.update("delete from clientes where id = ?", cliente);
        if (categoria != null) jdbc.update("delete from categorias where id = ?", categoria);
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    private ClienteRequestDTO datosCliente() {
        String dni = String.format("%08d", Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000));
        return new ClienteRequestDTO(dni, "Prueba", "Auditoria", requestId + "@example.test",
                null, null, true);
    }

    @Test
    void clienteConfirmadoTieneEventoSinDatosPersonales() {
        var datos = datosCliente();
        cliente = clientes.create(datos).getId();
        var evento = jdbc.queryForMap("select accion, entidad_id, detalle from auditoria_eventos where request_id = ?", requestId);
        assertThat(evento.get("ACCION")).isEqualTo("CLIENTE_REGISTRADO");
        assertThat(((Number) evento.get("ENTIDAD_ID")).longValue()).isEqualTo(cliente);
        assertThat(evento.get("DETALLE").toString()).doesNotContain(datos.getDni(), datos.getEmail());
    }

    @Test
    void categoriaPublicaSoloDespuesDeConfirmarLaTransaccion() {
        transacciones.executeWithoutResult(status -> {
            categoria = categorias.create(new CategoriaRequestDTO(requestId, "Prueba", true)).getId();
            assertThat(jdbc.queryForObject("select count(*) from auditoria_eventos where request_id = ?",
                    Integer.class, requestId)).isZero();
        });
        assertThat(jdbc.queryForObject("select accion from auditoria_eventos where request_id = ?",
                String.class, requestId)).isEqualTo("CATEGORIA_REGISTRADA");
    }

    @Test
    void categoriaRevertidaNoTieneEventoNiFila() {
        transacciones.executeWithoutResult(status -> {
            categorias.create(new CategoriaRequestDTO(requestId, "Rollback", true));
            status.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("select count(*) from categorias where nombre = ?",
                Integer.class, requestId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from auditoria_eventos where request_id = ?",
                Integer.class, requestId)).isZero();
    }
}
