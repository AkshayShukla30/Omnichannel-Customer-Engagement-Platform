package com.omnichannel.platform.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "crm")
public record CrmProperties(@DefaultValue("core-banking") String adapter, @DefaultValue Rest rest,
        @DefaultValue FileSource file) {

    public record Rest(@DefaultValue("http://localhost:8080/mock-crm") String baseUrl,
            @DefaultValue("json") String format) {
    }

    public record FileSource(@DefaultValue("classpath:data/customers.csv") String path) {
    }
}
