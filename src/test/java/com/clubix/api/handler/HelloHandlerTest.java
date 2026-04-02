package com.clubix.api.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class HelloHandlerTest {
    @Test
    void sayHello_returnsHelloWorld() {
        HelloHandler handler = new HelloHandler();

        // Router mínimo solo para invocar el handler
        RouterFunction<ServerResponse> rf = route(GET("/_test"), handler::sayHello);
        WebTestClient client = WebTestClient.bindToRouterFunction(rf).build();

        client.get().uri("/_test")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class).isEqualTo("Hello World!");
    }
}
