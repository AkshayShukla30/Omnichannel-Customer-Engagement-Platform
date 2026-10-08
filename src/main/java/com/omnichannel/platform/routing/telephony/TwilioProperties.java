package com.omnichannel.platform.routing.telephony;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telephony.twilio")
public record TwilioProperties(String accountSid, String authToken, String publicBaseUrl) {
}
