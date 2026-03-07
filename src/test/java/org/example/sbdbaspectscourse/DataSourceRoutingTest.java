package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.service.RoutingTestService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataSourceRoutingTest extends AbstractClusterTestcontainersSetupTest {

    @Autowired
    private RoutingTestService testService;

    @Test
    @Order(1)
    public void testRoutingToPrimaryForWrite() {
        boolean isRecovery = testService.isReplicaNode();
        Assertions.assertFalse(isRecovery, "Expected to route to Primary, but routed to Replica");
    }

    @Test
    @Order(2)
    public void testRoutingToReplicaForReadOnly() {
        boolean isRecovery = testService.isReplicaNodeReadOnly();
        Assertions.assertTrue(isRecovery, "Expected to route to Replica, but routed to Primary");
    }

    @Test
    @Order(3)
    public void testFailoverToPrimaryWhenReplicaIsDown() throws InterruptedException {
        replica.stop();
        System.out.println("Replica stopped. Waiting 3 seconds for TCP sockets to drop...");
        Thread.sleep(3000);
        boolean isRecovery = testService.isReplicaNodeReadOnly();
        Assertions.assertFalse(isRecovery, "Expected failover to Primary, but query failed or routed to Replica");
    }
}