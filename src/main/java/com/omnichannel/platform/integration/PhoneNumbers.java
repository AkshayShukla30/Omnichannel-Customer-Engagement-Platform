package com.omnichannel.platform.integration;

public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    // compare on the last 10 digits so +91 prefixes and spaces do not matter
    public static String normalize(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
    }
}
