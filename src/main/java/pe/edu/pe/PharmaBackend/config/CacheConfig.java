package pe.edu.pe.PharmaBackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    // La caché envuelve la transacción: se invalida cuando la escritura ya confirmó.
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                advertir("leer", cache);
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                advertir("guardar", cache);
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                advertir("invalidar", cache);
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                advertir("limpiar", cache);
            }
        };
    }

    private static void advertir(String operacion, Cache cache) {
        log.warn("Caché {} no disponible al {}; se continúa con Oracle", cache.getName(), operacion);
    }
}
