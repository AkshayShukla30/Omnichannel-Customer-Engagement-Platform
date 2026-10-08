package com.omnichannel.platform.integration.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.omnichannel.platform.integration.CustomerProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CrmParsersTest {

    private static final String JSON = """
            {"id":"CUST-1001","name":{"first":"Aarav","last":"Sharma"},
             "contact":{"email":"aarav@example.com","mobile":"+919800010001"},
             "segment":"PREMIER","account":{"number":"50100000011","balance":125430.50},"loans":1,"card":"PLATINUM"}""";

    @Test
    void jsonIsFlattenedIntoAProfile() {
        CustomerProfile profile = CrmJsonParser.parse(JSON, "rest-crm");

        assertThat(profile.customerId()).isEqualTo("CUST-1001");
        assertThat(profile.fullName()).isEqualTo("Aarav Sharma");
        assertThat(profile.phone()).isEqualTo("+919800010001");
        assertThat(profile.accountBalance()).isEqualByComparingTo(new BigDecimal("125430.50"));
        assertThat(profile.source()).isEqualTo("rest-crm");
    }

    @Test
    void xmlProducedFromJsonParsesToTheSameProfile() throws Exception {
        JsonNode node = new ObjectMapper().readTree(JSON);

        String xml = LegacyXmlWriter.toXml(node);

        CustomerProfile fromXml = CrmXmlParser.parse(xml, "rest-crm");
        CustomerProfile fromJson = CrmJsonParser.parse(JSON, "rest-crm");
        assertThat(fromXml.customerId()).isEqualTo(fromJson.customerId());
        assertThat(fromXml.fullName()).isEqualTo(fromJson.fullName());
        assertThat(fromXml.accountBalance()).isEqualByComparingTo(fromJson.accountBalance());
        assertThat(fromXml.cardType()).isEqualTo(fromJson.cardType());
    }

    @Test
    void xmlWithADoctypeIsRejected() {
        String hostile = "<?xml version=\"1.0\"?><!DOCTYPE customer [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<customer id=\"CUST-1\"><name><first>&x;</first></name></customer>";

        assertThatThrownBy(() -> CrmXmlParser.parse(hostile, "rest-crm"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void brokenXmlGivesAClearError() {
        assertThatThrownBy(() -> CrmXmlParser.parse("<customer>", "rest-crm"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid CRM XML");
    }
}
