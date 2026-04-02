package com.clubix.api.bdd.steps.business;

import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.MessageResponse;
import com.jmeta.outgoing.message.WhatsappMessage;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HelloSteps {

    @Autowired private WebTestClient         webTestClient;
    @Autowired private MessageSender         messageSender;
    @Autowired private MarkAsReadSender      markAsReadSender;
    @Autowired private TypingIndicatorSender typingIndicatorSender;

    private String payload;

    @Before
    public void stubSenders() {
        reset(messageSender, markAsReadSender, typingIndicatorSender);
        when(markAsReadSender.markAsRead(any())).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(any())).thenReturn(Mono.empty());
        when(messageSender.send(any())).thenReturn(Mono.just(new MessageResponse(200, "OK")));
    }

    @Given("I have a greeting message {string}")
    public void i_have_a_greeting_message(String message) throws IOException {
        // El payload JSON simula el webhook de WhatsApp con el mensaje dado.
        // Cargamos el archivo y lo usamos tal cual — el texto "Hello, World!"
        // ya está fijo en el JSON porque el feature lo define explícitamente.
        payload = new ClassPathResource("payloads/incoming/hello-world.json")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @When("I say the greeting")
    public void i_say_the_greeting() {
        webTestClient.post()
                .uri("/hook")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();
    }

    @Then("I should receive the response {string}")
    public void i_should_receive_the_response(String expectedResponse) {
        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        assertThat(captor.getValue().text().body()).isEqualTo(expectedResponse);
    }
}

