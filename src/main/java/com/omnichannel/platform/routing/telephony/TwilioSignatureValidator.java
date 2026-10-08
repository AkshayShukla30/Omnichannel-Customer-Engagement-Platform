package com.omnichannel.platform.routing.telephony;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class TwilioSignatureValidator {

    private TwilioSignatureValidator() {
    }

    public static boolean isValid(String authToken, String url, Map<String, String> params, String signature) {
        if (signature == null) {
            return false;
        }
        StringBuilder data = new StringBuilder(url);
        new TreeMap<>(params).forEach((key, value) -> data.append(key).append(value));
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String expected = Base64.getEncoder().encodeToString(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 not available", e);
        }
    }
}
