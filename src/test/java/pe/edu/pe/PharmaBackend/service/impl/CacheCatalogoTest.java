package pe.edu.pe.PharmaBackend.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pe.edu.pe.PharmaBackend.config.CacheConfig;
import pe.edu.pe.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.pe.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.pe.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.pe.PharmaBackend.entity.Categoria;
import pe.edu.pe.PharmaBackend.repository.CategoriaRepository;
import pe.edu.pe.PharmaBackend.repository.ProductoRepository;
import pe.edu.pe.PharmaBackend.service.service.CategoriaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(CacheCatalogoTest.Config.class)
class CacheCatalogoTest {
    @Configuration
    @Import({CacheConfig.class, CategoriaServiceImpl.class})
    static class Config {
        @Bean CacheManager cacheManager() { return new ConcurrentMapCacheManager("categorias", "productos"); }
        @Bean CategoriaRepository categorias() { return mock(CategoriaRepository.class); }
        @Bean ProductoRepository productos() { return mock(ProductoRepository.class); }
    }

    @Autowired CategoriaService service;
    @Autowired CategoriaRepository repository;
    @Autowired CacheManager caches;

    @BeforeEach
    void preparar() {
        reset(repository);
        caches.getCache("categorias").clear();
        caches.getCache("productos").clear();
        when(repository.findAll()).thenReturn(List.of());
    }

    @Test
    void segundaLecturaNoConsultaRepositorioYCrearInvalida() {
        service.readAll();
        service.readAll();
        verify(repository, times(1)).findAll();
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Categoría de prueba");
        request.setEstado(true);
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        service.create(request);
        service.readAll();
        verify(repository, times(2)).findAll();
    }

    @Test
    void renombrarCategoriaInvalidaTambienLosProductos() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Anterior");
        categoria.setEstado(true);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        caches.getCache("productos").put("0-10-nombre-asc-cat-null", "anterior");
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Nuevo");
        request.setEstado(true);
        service.update(1L, request);
        assertThat(caches.getCache("productos").get("0-10-nombre-asc-cat-null")).isNull();
    }

    @Test
    void paginaConFechasYDecimalesSeSerializaComoRedis() throws Exception {
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(1L);
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setFechaCreacion(java.time.LocalDateTime.of(2026, 11, 5, 9, 0));
        var pagina = new PaginaResponseDTO<>(List.of(producto), 0, 10, 1, 1, true);
        var bytes = new ByteArrayOutputStream();
        try (var salida = new ObjectOutputStream(bytes)) { salida.writeObject(pagina); }
        try (var entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            var recuperada = (PaginaResponseDTO<?>) entrada.readObject();
            assertThat(recuperada.getContenido()).hasSize(1);
            assertThat(((ProductoResponseDTO) recuperada.getContenido().get(0)).getPrecio()).isEqualByComparingTo("12.50");
        }
    }

    @Test
    void falloDeRedisNoInterrumpeLecturasNiEscrituras() {
        var handler = new CacheConfig().errorHandler();
        var cache = caches.getCache("categorias");
        var fallo = new IllegalStateException("Redis apagado");
        handler.handleCacheGetError(fallo, cache, "todas");
        handler.handleCachePutError(fallo, cache, "todas", List.of());
        handler.handleCacheEvictError(fallo, cache, "todas");
        handler.handleCacheClearError(fallo, cache);
    }
}
