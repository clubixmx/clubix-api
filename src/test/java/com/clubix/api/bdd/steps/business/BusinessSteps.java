package com.clubix.api.bdd.steps.business;

import com.clubix.api.incoming.OrchestratorService;
import com.clubix.api.tools.AccountTools;
import com.clubix.repository.CustomerPostgreSQLRepository;
import com.clubix.repository.entity.CustomerEntity;
import com.clubix.repository.reactive.CustomerEntityRepository;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.MessageResponse;
import com.jmeta.outgoing.message.WhatsappMessage;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BusinessSteps {

    private double scenarioBalance;

    @Autowired private R2dbcEntityTemplate          r2dbcTemplate;
    @Autowired private CustomerEntityRepository     entityRepository;
    @Autowired private CustomerPostgreSQLRepository customerRepository;
    @Autowired private WebTestClient                webTestClient;
    @Autowired private MessageSender                messageSender;
    @Autowired private MarkAsReadSender             markAsReadSender;
    @Autowired private TypingIndicatorSender        typingIndicatorSender;
    @Autowired private OrchestratorService          orchestratorService;
    @Autowired private AccountTools                 accountTools;

    @Before
    public void prepareScenario() {
        reset(customerRepository, messageSender, markAsReadSender, typingIndicatorSender, orchestratorService);
        entityRepository.deleteAll().block();

        when(markAsReadSender.markAsRead(any())).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(any())).thenReturn(Mono.empty());
        when(messageSender.send(any())).thenReturn(Mono.just(new MessageResponse(200, "OK")));

        // Delega al AccountTools real — misma cadena que usa CheckBalanceFlow.execute().
        // El manejo de errores (not found, unavailable) ya está dentro de AccountTools.
        doAnswer(inv -> {
            String text       = inv.getArgument(1);
            String customerId = text.split(" ")[1];
            return accountTools.checkBalance(customerId);
        }).when(orchestratorService).handle(any(), any());
    }

    // ── Givens ─────────────────────────────────────────────────────────────────

    @Given("a customer with ID {string} exists with balance {double}")
    public void a_customer_with_id_exists(String customerId, double balance) {
        this.scenarioBalance = balance;
        r2dbcTemplate.insert(
                CustomerEntity.builder()
                        .id(customerId)
                        .balance(balance)
                        .build()
        ).block();
    }

    @Given("no customer with ID {string} exists")
    public void no_customer_with_id_exists(String customerId) {
        // H2 ya está limpia desde @Before
    }

    @Given("the balance service is unavailable for customer ID {string}")
    public void the_balance_service_is_unavailable(String customerId) {
        doReturn(Mono.error(new RuntimeException("DB connection failed")))
                .when(customerRepository).findById(customerId);
    }

    // ── When ───────────────────────────────────────────────────────────────────

    @When("I query the balance for customer ID {string}")
    public void i_query_the_balance_for_customer_id(String customerId) throws IOException {
        String payload = new ClassPathResource("payloads/incoming/query_customer_balance.json")
                .getContentAsString(StandardCharsets.UTF_8)
                .replace("{customerId}", customerId);

        webTestClient.post()
                .uri("/hook")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();
    }

    // ── Thens ──────────────────────────────────────────────────────────────────

    @Then("the response should be {string}")
    public void the_response_should_be(String expectedResponse) {
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
            verify(messageSender).send(captor.capture());
            assertThat(captor.getValue().text().body()).isEqualTo(expectedResponse);
        });
    }

    @Then("the response should signal a not found error for customer ID {string}")
    public void the_response_should_signal_a_not_found_error(String customerId) {
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
            verify(messageSender).send(captor.capture());
            assertThat(captor.getValue().text().body()).containsIgnoringCase("not found");
        });
    }

    @Then("the response should signal a service unavailable error")
    public void the_response_should_signal_a_service_unavailable_error() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
            verify(messageSender).send(captor.capture());
            assertThat(captor.getValue().text().body()).containsIgnoringCase("no se pudo");
        });
    }
}
