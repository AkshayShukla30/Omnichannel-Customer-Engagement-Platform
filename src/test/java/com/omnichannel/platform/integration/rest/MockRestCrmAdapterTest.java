package com.omnichannel.platform.integration.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.omnichannel.platform.integration.CrmProperties;
import com.omnichannel.platform.integration.CustomerProfile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

class MockRestCrmAdapterTest {

    private static final String JSON = "{\"id\":\"CUST-1001\",\"name\":{\"first\":\"Aarav\",\"last\":\"Sharma\"},"
            + "\"contact\":{\"email\":\"a@example.com\",\"mobile\":\"+919800010001\"},\"segment\":\"PREMIER\","
            + "\"account\":{\"number\":\"1\",\"balance\":10.5},\"loans\":0,\"card\":\"CLASSIC\"}";

    private static final String XML = "<customer id=\"CUST-1001\"><name><first>Aarav</first><last>Sharma</last></name>"
            + "<contact><email>a@example.com</email><mobile>+919800010001</mobile></contact><segment>PREMIER</segment>"
            + "<account number=\"1\" balance=\"10.50\"/><loans>0</loans><card>CLASSIC</card></customer>";

    private MockRestServiceServer server;

    private MockRestCrmAdapter adapter(String format) {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://crm.test");
        server = MockRestServiceServer.bindTo(builder).build();
        CrmProperties props = new CrmProperties("rest-crm", new CrmProperties.Rest("http://crm.test", format),
                new CrmProperties.FileSource("x.csv"));
        return new MockRestCrmAdapter(builder.build(), props);
    }

    @Test
    void readsCustomerFromTheJsonApi() {
        MockRestCrmAdapter adapter = adapter("json");
        server.expect(requestTo("http://crm.test/customers/CUST-1001"))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

        Optional<CustomerProfile> profile = adapter.findById("CUST-1001");

        assertThat(profile).isPresent();
        assertThat(profile.get().fullName()).isEqualTo("Aarav Sharma");
        server.verify();
    }

    @Test
    void readsCustomerFromTheLegacyXmlApi() {
        MockRestCrmAdapter adapter = adapter("xml");
        server.expect(requestTo("http://crm.test/legacy/customers/CUST-1001"))
                .andExpect(header("Accept", MediaType.APPLICATION_XML_VALUE))
                .andRespond(withSuccess(XML, MediaType.APPLICATION_XML));

        Optional<CustomerProfile> profile = adapter.findById("CUST-1001");

        assertThat(profile).isPresent();
        assertThat(profile.get().segment()).isEqualTo("PREMIER");
    }

    @Test
    void phoneLookupSendsOnlyTheLastTenDigits() {
        MockRestCrmAdapter adapter = adapter("json");
        server.expect(requestTo("http://crm.test/customers/by-mobile/9800010001"))
                .andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

        assertThat(adapter.findByPhone("+91 98000 10001")).isPresent();
    }

    @Test
    void notFoundBecomesEmpty() {
        MockRestCrmAdapter adapter = adapter("json");
        server.expect(requestTo("http://crm.test/customers/NOPE")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.findById("NOPE")).isEmpty();
    }

    @Test
    void serverErrorsPropagateSoTheDeskCanShowAWarning() {
        MockRestCrmAdapter adapter = adapter("json");
        server.expect(requestTo("http://crm.test/customers/CUST-1001")).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.findById("CUST-1001")).isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void rejectsAnUnknownFormat() {
        CrmProperties props = new CrmProperties("rest-crm", new CrmProperties.Rest("http://crm.test", "yaml"),
                new CrmProperties.FileSource("x.csv"));

        assertThatThrownBy(() -> new MockRestCrmAdapter(RestClient.create(), props))
                .isInstanceOf(IllegalStateException.class);
    }
}
