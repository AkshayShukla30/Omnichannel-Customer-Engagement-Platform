package com.omnichannel.platform.integration.rest;

import com.omnichannel.platform.integration.CrmProperties;
import com.omnichannel.platform.integration.CustomerDataAdapter;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.integration.PhoneNumbers;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class MockRestCrmAdapter implements CustomerDataAdapter {

    public static final String TYPE = "rest-crm";

    private final RestClient client;
    private final boolean xml;

    public MockRestCrmAdapter(@Qualifier("crmRestClient") RestClient client, CrmProperties properties) {
        this.client = client;
        String format = properties.rest().format();
        if (!"json".equalsIgnoreCase(format) && !"xml".equalsIgnoreCase(format)) {
            throw new IllegalStateException("crm.rest.format must be json or xml, got '" + format + "'");
        }
        this.xml = "xml".equalsIgnoreCase(format);
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<CustomerProfile> findById(String customerId) {
        return fetch("/customers/{key}", customerId);
    }

    @Override
    public Optional<CustomerProfile> findByPhone(String phone) {
        return fetch("/customers/by-mobile/{key}", PhoneNumbers.normalize(phone));
    }

    private Optional<CustomerProfile> fetch(String path, String key) {
        String prefix = xml ? "/legacy" : "";
        try {
            String body = client.get()
                    .uri(prefix + path, key)
                    .accept(xml ? MediaType.APPLICATION_XML : MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            return Optional.of(xml ? CrmXmlParser.parse(body, TYPE) : CrmJsonParser.parse(body, TYPE));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
