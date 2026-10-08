package com.omnichannel.platform.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.omnichannel.platform.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class MockCrmIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void mockCrmServesJsonAndLegacyXml() throws Exception {
        mvc.perform(get("/mock-crm/customers/CUST-1001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name.first").value("Aarav"));

        mvc.perform(get("/mock-crm/legacy/customers/CUST-1001").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<first>Aarav</first>")));

        mvc.perform(get("/mock-crm/customers/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void mockCrmCanBeSearchedByMobile() throws Exception {
        mvc.perform(get("/mock-crm/customers/by-mobile/9800010002").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CUST-1002"));
    }
}
