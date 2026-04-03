package com.clubix.api.bdd;

import com.clubix.api.flows.account.balance.CheckBalanceFlow;
import com.clubix.api.incoming.OrchestratorService;
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
    @MockitoSpyBean public CustomerPostgreSQLRepository customerRepository;

    // Mockeamos CheckBalanceFlow para que Spring no intente crear el real,
    // que depende de CheckBalanceStateStore (Redis), no disponible en tests.
    // Como OrchestratorService también está mockeado, este mock nunca se invoca.
    @MockitoBean public CheckBalanceFlow     checkBalanceFlow;

    // OrchestratorService mockeado: evita wiring de ReactiveToolFlowRegistry
    // y sus dependencias de Redis/AI. BusinessSteps lo stubea con doAnswer
    // para delegar al QueryBalanceCommand real (cobertura E2E real).
    @MockitoBean public OrchestratorService  orchestratorService;

    // Evita llamadas reales a la API de WhatsApp/Meta
    @MockitoBean public MessageSender         messageSender;
    @MockitoBean public MarkAsReadSender      markAsReadSender;
    @MockitoBean public TypingIndicatorSender typingIndicatorSender;
}
