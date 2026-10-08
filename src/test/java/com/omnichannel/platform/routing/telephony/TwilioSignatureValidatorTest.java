package com.omnichannel.platform.routing.telephony;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TwilioSignatureValidatorTest {

    private static final String URL = "https://demo.example.com/api/telephony/twilio/menu";
    private static final Map<String, String> PARAMS = Map.of("CallSid", "CA1234567890", "Digits", "2", "From", "+919800010001");
    // computed with an independent python hmac implementation
    private static final String SIGNATURE = "tP4QdieC7WsqYLGQkan0oLX/nXc=";

    @Test
    void acceptsTheCorrectSignature() {
        assertThat(TwilioSignatureValidator.isValid("test_auth_token", URL, PARAMS, SIGNATURE)).isTrue();
    }

    @Test
    void rejectsTamperedParamsWrongTokenAndMissingSignature() {
        Map<String, String> tampered = Map.of("CallSid", "CA1234567890", "Digits", "3", "From", "+919800010001");

        assertThat(TwilioSignatureValidator.isValid("test_auth_token", URL, tampered, SIGNATURE)).isFalse();
        assertThat(TwilioSignatureValidator.isValid("other_token", URL, PARAMS, SIGNATURE)).isFalse();
        assertThat(TwilioSignatureValidator.isValid("test_auth_token", URL, PARAMS, null)).isFalse();
    }
}
