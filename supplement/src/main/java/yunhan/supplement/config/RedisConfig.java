package yunhan.supplement.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * ✅ Redis 전용 ObjectMapper 생성 (Bean 아님!)
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());              // LocalDate 등 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 타입 정보 포함해서 List<Diary>, List<UserEntity> 그대로 복원되게
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(mapper, null);
        return mapper;
    }

    /**
     * ✅ RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        var keySer = new StringRedisSerializer();
        var valSer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(keySer);
        template.setValueSerializer(valSer);
        template.setHashKeySerializer(keySer);
        template.setHashValueSerializer(valSer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * ✅ CacheManager 설정
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        var defaultSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(defaultSerializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("diariesByUser", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("friendsByUser", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}



//package yunhan.supplement.config;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//// yunhan/supplement/config/RedisConfig.java
//@Configuration
//@EnableCaching
//public class RedisConfig {
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory();
//    }
//
//    @Bean
//    public ObjectMapper redisObjectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        return mapper;
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf, ObjectMapper mapper) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(cf);
//        var keySer = new StringRedisSerializer();
//        var valSer = new GenericJackson2JsonRedisSerializer(mapper);
//        template.setKeySerializer(keySer);
//        template.setValueSerializer(valSer);
//        template.setHashKeySerializer(keySer);
//        template.setHashValueSerializer(valSer);
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper mapper) {
//        var defaultSerializer = new GenericJackson2JsonRedisSerializer(mapper);
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(defaultSerializer))
//                .disableCachingNullValues()
//                .entryTtl(Duration.ofMinutes(30));
//
//        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
//        cacheConfigs.put("diariesByUser", defaultConfig.entryTtl(Duration.ofMinutes(10)));
//        cacheConfigs.put("friendsByUser", defaultConfig.entryTtl(Duration.ofMinutes(60)));
//
//        return RedisCacheManager.builder(cf)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigs)
//                .build();
//    }
//}
