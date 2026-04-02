package com.clubix.api.incoming;

import com.clubix.repository.CustomerRepository;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.MessageResponse;
import com.jmeta.outgoing.message.WhatsappMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TextMessageProcessorIntegrationTest {

    private static final String WA_ID      = "5491112345678";
    private static final String MESSAGE_ID = "wamid-abc-123";
    private static final String TEXT       = "Hola!";

    @MockitoBean private MessageSender          messageSender;
    @MockitoBean private MarkAsReadSender       markAsReadSender;
    @MockitoBean private TypingIndicatorSender  typingIndicatorSender;
    @MockitoBean private CustomerRepository     customerRepository; // requerido por UseCaseConfig

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void stubSenders() {
        when(markAsReadSender.markAsRead(any())).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(any())).thenReturn(Mono.empty());
        when(messageSender.send(any())).thenReturn(Mono.just(new MessageResponse(200, "OK")));
    }

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void receiveTextMessage_shouldCallAllSendersWithCorrectArguments() throws IOException {
        postHook("payloads/incoming/text-message.json").expectStatus().isOk();

        verify(markAsReadSender).markAsRead(MESSAGE_ID);
        verify(typingIndicatorSender).send(MESSAGE_ID);

        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        WhatsappMessage sent = captor.getValue();
        assertThat(sent.to()).isEqualTo(WA_ID);
        assertThat(sent.type()).isEqualTo("text");
        assertThat(sent.text().body()).isEqualTo("Echo: " + TEXT);
    }

    // ── Failure paths ──────────────────────────────────────────────────────────

    @Test
    void receiveTextMessage_whenMarkAsReadFails_shouldStillReturn200AndNotCallSubsequentSenders() throws IOException {
        when(markAsReadSender.markAsRead(any()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        postHook("payloads/incoming/text-message.json").expectStatus().isOk();

        verify(markAsReadSender).markAsRead(MESSAGE_ID);
        verifyNoInteractions(typingIndicatorSender);
        verifyNoInteractions(messageSender);
    }

    @Test
    void receiveTextMessage_whenTypingIndicatorFails_shouldStillReturn200AndNotCallMessageSender() throws IOException {
        when(typingIndicatorSender.send(any()))
                .thenReturn(Mono.error(new RuntimeException("typing error")));

        postHook("payloads/incoming/text-message.json").expectStatus().isOk();

        verify(typingIndicatorSender).send(MESSAGE_ID);
        verifyNoInteractions(messageSender);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private WebTestClient.ResponseSpec postHook(String payloadPath) throws IOException {
        String body = new ClassPathResource(payloadPath)
                .getContentAsString(StandardCharsets.UTF_8);

        return webTestClient.post()
                .uri("/hook")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}



