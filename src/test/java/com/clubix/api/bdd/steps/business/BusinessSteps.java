package com.clubix.api.bdd.steps.business;

import com.clubix.entity.Customer;
import com.clubix.repository.CustomerRepository;
import com.clubix.usecase.QueryBalanceUseCase;
import com.clubix.usecase.model.response.QueryBalanceResponse;
import com.usecase.model.request.Request;
import com.usecase.model.request.RequestFactory;
import com.usecase.model.request.RequestFactoryImpl;
import com.usecase.model.response.Response;
import com.usecase.shared.ValidationException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class BusinessSteps {

    private static final Double EXPECTED_BALANCE = 130.0;
    private static final RequestFactory requestFactory =
            new RequestFactoryImpl("com.clubix.usecase.model.request");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QueryBalanceUseCase queryBalanceUseCase;

    private Mono<Response> response;

    @Before
    public void resetMocks() {
        reset(customerRepository);
    }

    // ── Givens ─────────────────────────────────────────────────────────────────

    @Given("a customer with ID {string} exists")
    public void a_customer_with_id_exists(String customerId) {
        Customer customer = Customer.builder()
                .id(customerId)
                .balance(EXPECTED_BALANCE)
                .build();
        when(customerRepository.findById(customerId)).thenReturn(Mono.just(customer));
    }

    @Given("no customer with ID {string} exists")
    public void no_customer_with_id_exists(String customerId) {
        when(customerRepository.findById(customerId)).thenReturn(Mono.empty());
    }

    @Given("the balance service is unavailable for customer ID {string}")
    public void the_balance_service_is_unavailable(String customerId) {
        when(customerRepository.findById(customerId))
                .thenReturn(Mono.error(new RuntimeException("DB connection failed")));
    }

    // ── When ───────────────────────────────────────────────────────────────────

    @When("I query the balance for customer ID {string}")
    public void i_query_the_balance_for_customer_id(String customerId) {
        Request request = requestFactory.get("QueryBalanceRequest", Map.of("customerId", customerId));
        response = queryBalanceUseCase.execute(request);
    }

    // ── Thens ──────────────────────────────────────────────────────────────────

    @Then("the response should contain the balance information for customer ID {string}")
    public void the_response_should_contain_the_balance_information_for_customer_id(String customerId) {
        StepVerifier.create(response)
                .assertNext(r -> {
                    Assertions.assertInstanceOf(QueryBalanceResponse.class, r);
                    QueryBalanceResponse qbr = (QueryBalanceResponse) r;
                    Assertions.assertEquals("SUCCESS", qbr.status);
                    Assertions.assertEquals(EXPECTED_BALANCE, qbr.balance);
                })
                .verifyComplete();
    }

    @Then("the response should signal a not found error for customer ID {string}")
    public void the_response_should_signal_a_not_found_error(String customerId) {
        StepVerifier.create(response)
                .verifyErrorMatches(e ->
                        e instanceof ValidationException &&
                        e.getMessage().contains(customerId));
    }

    @Then("the response should signal a service unavailable error")
    public void the_response_should_signal_a_service_unavailable_error() {
        StepVerifier.create(response)
                .verifyErrorMatches(e ->
                        e instanceof RuntimeException &&
                        "DB connection failed".equals(e.getMessage()));
    }
}

