package com.omnichannel.platform.integration.rest;

import com.omnichannel.platform.integration.PhoneNumbers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
class MockCrmStore {

    private final List<JsonNode> customers = new ArrayList<>();

    MockCrmStore(ObjectMapper mapper) {
        try (InputStream in = new ClassPathResource("mock-crm/customers.json").getInputStream()) {
            mapper.readTree(in).forEach(customers::add);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load mock CRM data", e);
        }
    }

    Optional<JsonNode> findById(String id) {
        return customers.stream().filter(c -> c.path("id").asText().equals(id)).findFirst();
    }

    Optional<JsonNode> findByMobile(String mobile) {
        String wanted = PhoneNumbers.normalize(mobile);
        return customers.stream()
                .filter(c -> PhoneNumbers.normalize(c.path("contact").path("mobile").asText()).equals(wanted))
                .findFirst();
    }
}
