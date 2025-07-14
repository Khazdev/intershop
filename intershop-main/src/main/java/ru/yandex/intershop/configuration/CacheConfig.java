package ru.yandex.intershop.configuration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.intershop.model.Item;

import java.util.List;

@Configuration
public class CacheConfig {

    @Bean()
    public ReactiveRedisTemplate<String, List<Item>> itemListReactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaType type = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Item.class);

        Jackson2JsonRedisSerializer<List<Item>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, type);

        RedisSerializationContext<String, List<Item>> context = RedisSerializationContext
                .<String, List<Item>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean()
    public ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        JavaType type = objectMapper.constructType(Item.class);
        Jackson2JsonRedisSerializer<Item> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, type);

        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}