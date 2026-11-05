package pe.edu.pe.PharmaBackend.service.impl;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.pe.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.DetalleVentaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.pe.PharmaBackend.service.service.CategoriaService;
import pe.edu.pe.PharmaBackend.service.service.VentaService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.data.redis.database=15", "spring.jpa.hibernate.ddl-auto=validate",
        "logging.level.org.hibernate.SQL=OFF", "spring.jpa.show-sql=false",
        "logging.level.org.springframework.cache.interceptor=TRACE"})
@Transactional
@Rollback
class CacheOracleRollbackTest {
    @Autowired CategoriaService categorias;
    @Autowired VentaService ventas;
    @Autowired JdbcTemplate jdbc;
    @Autowired CacheManager caches;

    // Base Redis separada de la aplicación. Todas las escrituras Oracle se revierten.
    @BeforeEach
    @AfterEach
    void limpiarCacheDePruebas() {
        caches.getCache("categorias").clear();
        caches.getCache("productos").clear();
    }

    @Test
    void crearCategoriaInvalidaRedisYExisteDentroDeLaTransaccion() {
        assertThat(caches).isInstanceOf(org.springframework.data.redis.cache.RedisCacheManager.class);
        assertThat(org.springframework.aop.support.AopUtils.isAopProxy(categorias)).isTrue();
        caches.getCache("categorias").put("todas", List.of());
        var request = new CategoriaRequestDTO("S13-" + UUID.randomUUID().toString().substring(0, 8),
                "Prueba con rollback", true);
        var creada = categorias.create(request);
        assertThat(caches.getCache("categorias").get("todas")).isNull();
        assertThat(jdbc.queryForObject("select count(*) from categorias where id = ?", Long.class, creada.getId())).isEqualTo(1);
    }

    @Test
    void ventaDescuentaStockEInvalidaProductosConRollback() {
        Long cliente = jdbc.queryForObject("select min(id) from clientes where estado = 1", Long.class);
        Long producto = jdbc.queryForObject("select min(id) from producto where estado = 1 and stock >= 1", Long.class);
        assertThat(cliente).as("Debe existir un cliente activo para la prueba").isNotNull();
        assertThat(producto).as("Debe existir un producto activo con stock").isNotNull();
        Integer antes = jdbc.queryForObject("select stock from producto where id = ?", Integer.class, producto);
        caches.getCache("productos").put("0-10-nombre-asc-cat-null", List.of());
        var venta = ventas.registrar(new VentaRequestDTO(cliente, List.of(new DetalleVentaRequestDTO(producto, 1))));
        assertThat(venta.getId()).isNotNull();
        assertThat(jdbc.queryForObject("select stock from producto where id = ?", Integer.class, producto)).isEqualTo(antes - 1);
        assertThat(caches.getCache("productos").get("0-10-nombre-asc-cat-null")).isNull();
    }
}
