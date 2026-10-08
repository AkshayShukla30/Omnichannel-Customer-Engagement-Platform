package com.omnichannel.platform.routing.telephony;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.routing.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "telephony.provider", havingValue = "twilio")
public class TwilioTelephonyProvider implements TelephonyProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioTelephonyProvider.class);

    private final RestClient client;

    public TwilioTelephonyProvider(TwilioProperties properties) {
        if (!StringUtils.hasText(properties.accountSid()) || !StringUtils.hasText(properties.authToken())
                || !StringUtils.hasText(properties.publicBaseUrl())) {
            throw new IllegalStateException(
                    "telephony.provider=twilio needs TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_PUBLIC_BASE_URL");
        }
        this.client = RestClient.builder()
                .baseUrl("https://api.twilio.com/2010-04-01/Accounts/" + properties.accountSid())
                .defaultHeaders(h -> h.setBasicAuth(properties.accountSid(), properties.authToken()))
                .build();
    }

    @Override
    public void connect(Interaction interaction, Agent agent) {
        if (!StringUtils.hasText(agent.getPhone())) {
            log.warn("Agent {} has no phone number, cannot bridge call {}", agent.getUsername(), interaction.getId());
            return;
        }
        String number = agent.getPhone().replaceAll("[^+0-9]", "");
        updateCall(interaction.getExternalRef(), "Twiml", "<Response><Dial>" + number + "</Dial></Response>");
    }

    @Override
    public void hangUp(Interaction interaction) {
        try {
            updateCall(interaction.getExternalRef(), "Status", "completed");
        } catch (RestClientException e) {
            // the caller usually hung up first, nothing to clean up then
            log.info("Could not end call for interaction {}: {}", interaction.getId(), e.getMessage());
        }
    }

    private void updateCall(String callSid, String field, String value) {
        if (!StringUtils.hasText(callSid)) {
            return;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(field, value);
        client.post()
                .uri("/Calls/{sid}.json", callSid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
