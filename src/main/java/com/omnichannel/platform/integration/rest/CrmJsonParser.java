package com.omnichannel.platform.integration.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnichannel.platform.integration.CustomerProfile;

final class CrmJsonParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CrmJsonParser() {
    }

    static CustomerProfile parse(String json, String source) {
        try {
            return fromNode(MAPPER.readTree(json), source);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid CRM JSON", e);
        }
    }

    // the CRM nests name and contact, our profile is flat
    static CustomerProfile fromNode(JsonNode n, String source) {
        String fullName = (n.path("name").path("first").asText("") + " " + n.path("name").path("last").asText("")).trim();
        return new CustomerProfile(n.path("id").asText(), fullName, n.path("contact").path("email").asText(null),
                n.path("contact").path("mobile").asText(null), n.path("segment").asText(null),
                n.path("account").path("number").asText(null), n.path("account").path("balance").decimalValue(),
                n.path("loans").asInt(0), n.path("card").asText(null), source);
    }
}
