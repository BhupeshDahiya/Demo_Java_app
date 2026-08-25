package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointReturnsUp() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/health",
                        Map.class
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void homeEndpointReturnsCorrectMessage() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/",
                        Map.class
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "DevOps Demo App Running",
                response.getBody().get("message")
        );

        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void logsTestEndpointReturnsSuccessMessage() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/logs-test",
                        String.class
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "Logs generated successfully",
                response.getBody()
        );
    }

    @Test
    void validateEndpointAcceptsValidInput() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/validate?input=hello",
                        Map.class
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals("valid", response.getBody().get("status"));
        assertEquals("hello", response.getBody().get("input"));
    }

    @Test
    void validateEndpointTrimsInput() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/validate?input=+hello+",
                        Map.class
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals("valid", response.getBody().get("status"));
        assertEquals("hello", response.getBody().get("input"));
    }
}
