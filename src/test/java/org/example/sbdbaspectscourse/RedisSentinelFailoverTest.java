package org.example.sbdbaspectscourse;

import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolvers;
import io.lettuce.core.resource.MappingSocketAddressResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;

@DataRedisTest
@Import(RedisSentinelFailoverTest.LettuceTestConfig.class)
class RedisSentinelFailoverTest {

    static Network network = Network.newNetwork();

    static GenericContainer<?> master = new GenericContainer<>("redis:8.6")
            .withNetwork(network)
            .withNetworkAliases("redis-master")
            .withExposedPorts(6379)
            .withCommand("redis-server",
                    "--replica-announce-ip", "redis-master",
                    "--replica-announce-port", "6379");

    static GenericContainer<?> replica = new GenericContainer<>("redis:8.6")
            .withNetwork(network)
            .withNetworkAliases("redis-replica")
            .withExposedPorts(6379)
            .dependsOn(master)
            .withCommand("redis-server",
                    "--replicaof", "redis-master", "6379",
                    "--replica-announce-ip", "redis-replica",
                    "--replica-announce-port", "6379");

    static GenericContainer<?> sentinel = new GenericContainer<>("redis:8.6")
            .withNetwork(network)
            .withNetworkAliases("redis-sentinel")
            .withExposedPorts(26379)
            .dependsOn(master, replica)
            .withCommand("sh", "-c",
                    "echo 'port 26379' > /tmp/sentinel.conf && " +
                            "echo 'dir /tmp' >> /tmp/sentinel.conf && " +
                            "echo 'sentinel resolve-hostnames yes' >> /tmp/sentinel.conf && " +
                            "echo 'sentinel announce-hostnames yes' >> /tmp/sentinel.conf && " +
                            "echo 'sentinel monitor mymaster redis-master 6379 1' >> /tmp/sentinel.conf && " +
                            "echo 'sentinel down-after-milliseconds mymaster 2000' >> /tmp/sentinel.conf && " +
                            "echo 'sentinel failover-timeout mymaster 10000' >> /tmp/sentinel.conf && " +
                            "redis-sentinel /tmp/sentinel.conf");

    static {
        master.start();
        replica.start();
        sentinel.start();
    }

    @TestConfiguration
    static class LettuceTestConfig {
        @Bean(destroyMethod = "shutdown")
        public ClientResources clientResources() {
            Map<HostAndPort, HostAndPort> mapping = new HashMap<>();

            mapping.put(HostAndPort.of("redis-master", 6379), HostAndPort.of(master.getHost(), master.getMappedPort(6379)));
            mapping.put(HostAndPort.of("redis-replica", 6379), HostAndPort.of(replica.getHost(), replica.getMappedPort(6379)));

            MappingSocketAddressResolver resolver = MappingSocketAddressResolver.create(
                    DnsResolvers.UNRESOLVED,
                    hostAndPort -> mapping.getOrDefault(hostAndPort, hostAndPort)
            );

            return ClientResources.builder()
                    .socketAddressResolver(resolver)
                    .build();
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.sentinel.master", () -> "mymaster");
        registry.add("spring.data.redis.sentinel.nodes", () -> sentinel.getHost() + ":" + sentinel.getMappedPort(26379));
        registry.add("spring.data.redis.timeout", () -> "2s");
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void testFailoverSurvivesMasterNodeCrash() throws InterruptedException {
        System.out.println("Waiting for Redis Cluster to stabilize (SYNC and Sentinel discovery)...");
        Thread.sleep(2000);

        redisTemplate.opsForValue().set("sentinel-test-key", "data-before-crash");
        Assertions.assertEquals("data-before-crash", redisTemplate.opsForValue().get("sentinel-test-key"));

        System.out.println("Freezing master node to simulate hard crash...");
        master.getDockerClient().pauseContainerCmd(master.getContainerId()).exec();

        boolean failoverSuccessful = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            try {
                if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                    ((LettuceConnectionFactory) redisConnectionFactory).resetConnection();
                }
                redisTemplate.opsForValue().set("sentinel-test-key-2", "data-after-crash");
                failoverSuccessful = true;
                System.out.println("Failover completed successfully after " + (i + 1) + " seconds!");
                break;
            } catch (Exception e) {
                System.out.println("Waiting for Sentinel to promote replica... " + e.getMessage());
            }
        }

        if (!failoverSuccessful) {
            System.err.println("=== SENTINEL LOGS ===");
            System.err.println(sentinel.getLogs());
            System.err.println("=====================");
        }

        Assertions.assertTrue(failoverSuccessful, "Failover did not complete in time!");
        Assertions.assertEquals("data-after-crash", redisTemplate.opsForValue().get("sentinel-test-key-2"));
    }
}