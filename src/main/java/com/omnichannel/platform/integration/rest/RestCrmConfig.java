package com.omnichannel.platform.integration.rest;

import com.omnichannel.platform.integration.CrmProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class RestCrmConfig {

    @Bean
    RestClient crmRestClient(CrmProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        return RestClient.builder().baseUrl(properties.rest().baseUrl()).requestFactory(factory).build();
    }
}
