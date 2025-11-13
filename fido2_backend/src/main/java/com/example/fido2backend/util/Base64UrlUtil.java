package com.example.fido2backend.util;

import java.util.Base64;

/**
 * Utility to convert between bytes and base64url (no padding).
 */
public final class Base64UrlUtil {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private Base64UrlUtil() {}

    // PUBLIC_INTERFACE
    public static String toBase64Url(byte[] bytes) {
        /** Converts bytes to base64url string without padding. */
        if (bytes == null) return null;
        return URL_ENCODER.encodeToString(bytes);
    }

    // PUBLIC_INTERFACE
    public static byte[] fromBase64Url(String s) {
        /** Converts base64url string to bytes. Accepts no padding. */
        if (s == null) return null;
        return URL_DECODER.decode(s);
    }
}
