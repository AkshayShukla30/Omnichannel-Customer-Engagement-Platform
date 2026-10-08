package com.omnichannel.platform.integration;

import java.util.Optional;

public interface CustomerDataAdapter {

    String type();

    Optional<CustomerProfile> findById(String customerId);

    Optional<CustomerProfile> findByPhone(String phone);
}
