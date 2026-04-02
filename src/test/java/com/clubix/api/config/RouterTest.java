package com.clubix.api.config;


import com.clubix.api.handler.HelloHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class RouterTest {
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        // Arrange: wire the router to the handler
        HelloHandler handler = new HelloHandler();
        RouterFunction<ServerResponse> routes = route(GET("/hello"), handler::sayHello);

        // Bind a client directly to the RouterFunction (no full Spring context)
        client = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Test
    void getHello_returnsHelloWorld() {
        client.get()
                .uri("/hello")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class).isEqualTo("Hello World!");
    }
}
