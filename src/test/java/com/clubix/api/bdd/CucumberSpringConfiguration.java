package com.clubix.api.bdd;

import com.clubix.repository.CustomerPostgreSQLRepository;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    // Spy sobre el CustomerPostgreSQLRepository real (H2 en tests).
    // Permite llamadas reales para happy paths y stubbing para "service unavailable".
    @MockitoSpyBean public CustomerPostgreSQLRepository customerRepository;

    // Evita llamadas reales a la API de WhatsApp/Meta
    @MockitoBean public MessageSender         messageSender;
    @MockitoBean public MarkAsReadSender      markAsReadSender;
    @MockitoBean public TypingIndicatorSender typingIndicatorSender;
}
