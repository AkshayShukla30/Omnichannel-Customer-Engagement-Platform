package com.omnichannel.platform.routing;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "routing")
public record RoutingProperties(@DefaultValue("longest-idle") String strategy,
        @DefaultValue("20s") Duration heartbeatTimeout) {
}
