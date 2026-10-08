package com.omnichannel.platform.integration;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(CustomerDataAdapterFactory.class);

    private final Map<String, CustomerDataAdapter> byType = new TreeMap<>();
    private final CustomerDataAdapter current;

    public CustomerDataAdapterFactory(List<CustomerDataAdapter> adapters, CrmProperties properties) {
        adapters.forEach(adapter -> byType.put(adapter.type(), adapter));
        this.current = byType.get(properties.adapter());
        if (current == null) {
            throw new IllegalStateException(
                    "Unknown crm.adapter '" + properties.adapter() + "', available: " + byType.keySet());
        }
        log.info("CRM adapter in use: {}", current.type());
    }

    public CustomerDataAdapter current() {
        return current;
    }

    public List<String> availableTypes() {
        return List.copyOf(byType.keySet());
    }
}
