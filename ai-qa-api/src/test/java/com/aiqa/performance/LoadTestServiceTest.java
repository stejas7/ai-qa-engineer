package com.aiqa.performance;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class LoadTestServiceTest {
    private HttpServer server;

    @AfterEach void stop() { if (server != null) server.stop(0); }

    @Test void measuresLatencyThroughputAndPassesSloForHealthyTarget() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/health", exchange -> { exchange.sendResponseHeaders(200, -1); exchange.close(); });
        server.start();
        String target = "http://127.0.0.1:" + server.getAddress().getPort() + "/health";
        var result = new LoadTestService().run(new LoadTestService.LoadTestRequest(target, 12, 3, 2000, 0));
        assertEquals(12, result.requests());
        assertEquals(0, result.failures());
        assertTrue(result.sloPassed());
        assertTrue(result.throughputPerSecond() > 0);
        assertTrue(result.p99Ms() >= result.p50Ms());
    }

    @Test void capsWorkloadAndRejectsNonHttpTargets() {
        var service = new LoadTestService();
        assertThrows(IllegalArgumentException.class,
                () -> service.run(new LoadTestService.LoadTestRequest("file:///tmp/test", 1, 1, 100, 0)));
    }
}
