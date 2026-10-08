package com.omnichannel.platform.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerProfile(String customerId, String fullName, String email, String phone, String segment,
        String accountNumber, BigDecimal accountBalance, int activeLoans, String cardType, String source) {

    public CustomerProfile withSource(String newSource) {
        return new CustomerProfile(customerId, fullName, email, phone, segment, accountNumber, accountBalance,
                activeLoans, cardType, newSource);
    }
}
