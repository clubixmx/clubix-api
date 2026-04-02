package com.clubix.api.bdd.steps.business;

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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BusinessSteps {

    private static final Double EXPECTED_BALANCE = 130.0;

    @Autowired private R2dbcEntityTemplate          r2dbcTemplate;
    @Autowired private CustomerEntityRepository     entityRepository;
    @Autowired private CustomerPostgreSQLRepository customerRepository;
    @Autowired private WebTestClient                webTestClient;
    @Autowired private MessageSender                messageSender;
    @Autowired private MarkAsReadSender             markAsReadSender;
    @Autowired private TypingIndicatorSender        typingIndicatorSender;

    @Before
    public void prepareScenario() {
        reset(customerRepository, messageSender, markAsReadSender, typingIndicatorSender);
        entityRepository.deleteAll().block();

        when(markAsReadSender.markAsRead(any())).thenReturn(Mono.empty());
        when(typingIndicatorSender.send(any())).thenReturn(Mono.empty());
        when(messageSender.send(any())).thenReturn(Mono.just(new MessageResponse(200, "OK")));
    }

    // ── Givens ─────────────────────────────────────────────────────────────────

    @Given("a customer with ID {string} exists")
    public void a_customer_with_id_exists(String customerId) {
        r2dbcTemplate.insert(
                CustomerEntity.builder()
                        .id(customerId)
                        .balance(EXPECTED_BALANCE)
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

    @Then("the response should contain the balance information for customer ID {string}")
    public void the_response_should_contain_the_balance_information_for_customer_id(String customerId) {
        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        String body = captor.getValue().text().body();
        assertThat(body)
                .contains(customerId)
                .contains(String.valueOf(EXPECTED_BALANCE));
    }

    @Then("the response should signal a not found error for customer ID {string}")
    public void the_response_should_signal_a_not_found_error(String customerId) {
        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        assertThat(captor.getValue().text().body())
                .containsIgnoringCase("not found");
    }

    @Then("the response should signal a service unavailable error")
    public void the_response_should_signal_a_service_unavailable_error() {
        ArgumentCaptor<WhatsappMessage> captor = ArgumentCaptor.forClass(WhatsappMessage.class);
        verify(messageSender).send(captor.capture());

        assertThat(captor.getValue().text().body())
                .containsIgnoringCase("unavailable");
    }
}
