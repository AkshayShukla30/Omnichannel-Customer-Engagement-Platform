package com.omnichannel.platform.integration.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-crm")
@Tag(name = "Mock CRM", description = "Stand-in for a third party CRM: JSON api plus a legacy XML api")
class MockCrmController {

    private final MockCrmStore store;

    MockCrmController(MockCrmStore store) {
        this.store = store;
    }

    @GetMapping(value = "/customers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> json(@PathVariable String id) {
        return ResponseEntity.of(store.findById(id));
    }

    @GetMapping(value = "/customers/by-mobile/{mobile}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> jsonByMobile(@PathVariable String mobile) {
        return ResponseEntity.of(store.findByMobile(mobile));
    }

    // plain 404 without a body: an error body would be negotiated as xml and fail
    @GetMapping(value = "/legacy/customers/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> xml(@PathVariable String id) {
        return ResponseEntity.of(store.findById(id).map(LegacyXmlWriter::toXml));
    }

    @GetMapping(value = "/legacy/customers/by-mobile/{mobile}", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> xmlByMobile(@PathVariable String mobile) {
        return ResponseEntity.of(store.findByMobile(mobile).map(LegacyXmlWriter::toXml));
    }
}
