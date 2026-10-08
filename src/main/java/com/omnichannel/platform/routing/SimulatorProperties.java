package com.omnichannel.platform.routing;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(List<String> customerRefs) {

    public SimulatorProperties {
        if (customerRefs == null || customerRefs.isEmpty()) {
            customerRefs = List.of("CUST-1001");
        }
    }
}
