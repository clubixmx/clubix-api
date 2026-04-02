package com.clubix.api.bdd;

import com.clubix.repository.CustomerRepository;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    // ── Repositorio: no está en el scan de producción ──────────────────────────
    @MockitoBean public CustomerRepository customerRepository;

    // ── Senders: evita llamadas reales a la API de WhatsApp/Meta ──────────────
    @MockitoBean public MessageSender         messageSender;
    @MockitoBean public MarkAsReadSender      markAsReadSender;
    @MockitoBean public TypingIndicatorSender typingIndicatorSender;
}
