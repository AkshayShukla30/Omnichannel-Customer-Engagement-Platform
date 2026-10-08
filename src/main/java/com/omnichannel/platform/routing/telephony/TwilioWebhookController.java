package com.omnichannel.platform.routing.telephony;

import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import com.omnichannel.platform.integration.CustomerDataAdapterFactory;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.routing.NewInteraction;
import com.omnichannel.platform.routing.RoutingEngine;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Hidden
@RestController
@RequestMapping("/api/telephony/twilio")
@ConditionalOnProperty(name = "telephony.provider", havingValue = "twilio")
public class TwilioWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);
    private static final String BASE_PATH = "/api/telephony/twilio";

    private final TwilioProperties properties;
    private final RoutingEngine routing;
    private final CustomerDataAdapterFactory adapters;

    public TwilioWebhookController(TwilioProperties properties, RoutingEngine routing,
            CustomerDataAdapterFactory adapters) {
        this.properties = properties;
        this.routing = routing;
        this.adapters = adapters;
    }

    @PostMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String greet(@RequestParam Map<String, String> params,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature) {
        verify("/voice", params, signature);
        return "<Response><Gather input=\"dtmf\" numDigits=\"1\" action=\"" + publicUrl("/menu") + "\">"
                + "<Say>Press 1 for loans, 2 for cards, 3 to register a complaint, or 4 for anything else.</Say>"
                + "</Gather><Say>We did not get your choice. Goodbye.</Say></Response>";
    }

    @PostMapping(value = "/menu", produces = MediaType.APPLICATION_XML_VALUE)
    public String queueCall(@RequestParam Map<String, String> params,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature) {
        verify("/menu", params, signature);
        Skill skill = switch (params.getOrDefault("Digits", "")) {
            case "1" -> Skill.LOAN;
            case "2" -> Skill.CARD;
            case "3" -> Skill.COMPLAINT;
            default -> Skill.GENERAL;
        };
        String customerRef = findCustomerRef(params.getOrDefault("From", ""));
        routing.enqueue(new NewInteraction(Channel.CALL, skill, 2, customerRef, params.get("CallSid")));
        return "<Response><Say>Thank you. Please hold while we find an agent.</Say><Pause length=\"600\"/></Response>";
    }

    private String findCustomerRef(String callerNumber) {
        try {
            return adapters.current().findByPhone(callerNumber).map(CustomerProfile::customerId).orElse("UNKNOWN");
        } catch (RuntimeException e) {
            log.warn("Caller lookup failed: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    private void verify(String path, Map<String, String> params, String signature) {
        if (!TwilioSignatureValidator.isValid(properties.authToken(), publicUrl(path), params, signature)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Twilio signature");
        }
    }

    private String publicUrl(String path) {
        String base = properties.publicBaseUrl().replaceAll("/+$", "");
        return base + BASE_PATH + path;
    }
}
