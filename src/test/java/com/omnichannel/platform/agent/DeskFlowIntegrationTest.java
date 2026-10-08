package com.omnichannel.platform.agent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.omnichannel.platform.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

class DeskFlowIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void agentHandlesAnInteractionFromQueueToDisposition() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"priya\",\"password\":\"agent123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mvc.perform(post("/api/simulator/interactions").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":1,\"skill\":\"LOAN\",\"channel\":\"CALL\",\"priority\":1,\"customerRef\":\"CUST-1001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ASSIGNED"));

        String interactionId = mvc.perform(get("/api/desk/state").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interaction.skill").value("LOAN"))
                .andExpect(jsonPath("$.customer.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.customer.source").value("core-banking"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(com.jayway.jsonpath.JsonPath.read(interactionId, "$.interaction.id").toString());

        mvc.perform(post("/api/desk/interactions/" + id + "/notes").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Asked about EMI holiday\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/desk/interactions/" + id + "/close").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disposition\":\"RESOLVED\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/desk/state").session(session))
                .andExpect(jsonPath("$.interaction").doesNotExist())
                .andExpect(jsonPath("$.agent.status").value("AVAILABLE"));

        mvc.perform(get("/api/reports/dispositions?days=1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.disposition=='RESOLVED')].total").isNotEmpty());

        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());
    }

    @Test
    void protectedEndpointsNeedALogin() throws Exception {
        mvc.perform(get("/api/desk/state")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/reports/queue-length")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"priya\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRequestBodyGivesFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }
}
