package com.clubix.api.incoming;

import com.jmeta.incoming.message.IncomingTextMessage;
import com.jmeta.incoming.message.Message;
import com.jmeta.incoming.message.Profile;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.MessageResponse;
import com.jmeta.outgoing.message.WhatsappMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextMessageProcessorTest {

    @Mock private MessageSender          messageSender;
    @Mock private MarkAsReadSender       markAsReadSender;
    @Mock private TypingIndicatorSender  typingIndicatorSender;
    @Mock private OrchestratorService    orchestratorService;

    private TextMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TextMessageProcessor(
                messageSender, markAsReadSender, typingIndicatorSender, orchestratorService);
    }

    @Test
    void process_shouldCallAllSendersWithCorrectArguments() {
        // Given
        String messageId           = "msg-123";
        String waId                = "5491112345678";
        String text                = "Hello!";
        String orchestratorResponse = "Hola! ¿En qué puedo ayudarte?";

        IncomingTextMessage incoming = new IncomingTextMessage(
                new Profile("John Doe", waId),
                new Message(waId, messageId),
                "text",
                text
        );

        when(markAsReadSender.markAsRead(messageId)).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(messageId)).thenReturn(Mono.empty());
        when(orchestratorService.handle(waId, text)).thenReturn(Mono.just(orchestratorResponse));
        when(messageSender.send(any(WhatsappMessage.class)))
                .thenReturn(Mono.just(new MessageResponse(200, "OK")));

        // When
        processor.process(incoming);

        // Then
        verify(markAsReadSender).markAsRead(messageId);
        verify(typingIndicatorSender).send(messageId);
        verify(orchestratorService).handle(waId, text);

        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        WhatsappMessage sent = captor.getValue();
        assertThat(sent.to()).isEqualTo(waId);
        assertThat(sent.type()).isEqualTo("text");
        assertThat(sent.text().body()).isEqualTo(orchestratorResponse);
    }

    @Test
    void process_whenMarkAsReadFails_shouldNotCallSubsequentSenders() {
        // Given
        String messageId = "msg-456";
        IncomingTextMessage incoming = new IncomingTextMessage(
                new Profile("Jane", "5491199999999"),
                new Message("5491199999999", messageId),
                "text",
                "Hi"
        );

        when(markAsReadSender.markAsRead(messageId))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // When
        processor.process(incoming);

        // Then
        verify(markAsReadSender).markAsRead(messageId);
        verifyNoInteractions(typingIndicatorSender);
        verifyNoInteractions(orchestratorService);
        verifyNoInteractions(messageSender);
    }

    @Test
    void process_whenTypingIndicatorFails_shouldNotCallMessageSender() {
        // Given
        String messageId = "msg-789";
        IncomingTextMessage incoming = new IncomingTextMessage(
                new Profile("Bob", "5491188888888"),
                new Message("5491188888888", messageId),
                "text",
                "Hey"
        );

        when(markAsReadSender.markAsRead(messageId)).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(messageId))
                .thenReturn(Mono.error(new RuntimeException("Typing indicator error")));

        // When
        processor.process(incoming);

        // Then
        verify(markAsReadSender).markAsRead(messageId);
        verify(typingIndicatorSender).send(messageId);
        verifyNoInteractions(orchestratorService);
        verifyNoInteractions(messageSender);
    }
}
