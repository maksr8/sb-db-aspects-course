package org.example.sbdbaspectscourse.config;

import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolvers;
import io.lettuce.core.resource.MappingSocketAddressResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Profile("!test")
@Configuration
public class RedisSentinelConfig {

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        Map<HostAndPort, HostAndPort> mapping = new HashMap<>();
        mapping.put(HostAndPort.of("redis-master", 6379), HostAndPort.of("localhost", 6379));
        mapping.put(HostAndPort.of("redis-replica", 6379), HostAndPort.of("localhost", 6380));

        MappingSocketAddressResolver resolver = MappingSocketAddressResolver.create(
                DnsResolvers.UNRESOLVED,
                hostAndPort -> mapping.getOrDefault(hostAndPort, hostAndPort)
        );

        return ClientResources.builder()
                .socketAddressResolver(resolver)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("localhost", 26379);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }
}