package pe.edu.pe.PharmaBackend.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisCacheWriterConfig {
    @Bean
    RedisCacheManagerBuilderCustomizer escriturasInmediatas(RedisConnectionFactory connectionFactory) {
        // Spring Data Redis 4 puede diferir put/clear con Lettuce. Esperar evita
        // que un put anterior termine después de invalidar y reponga datos viejos.
        return builder -> builder.cacheWriter(RedisCacheWriter.create(connectionFactory,
                configurer -> configurer.immediateWrites()));
    }
}
