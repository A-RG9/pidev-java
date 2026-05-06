package com.wellcare.javafx.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Service for Two-Factor Authentication (TOTP).
 */
public class TotpService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP = 30; // 30 seconds
    private static final String CRYPTO_ALGO = "HmacSHA1";

    /**
     * Generates a new Base32 secret key.
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[10]; // 80 bits is standard for TOTP secrets
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    private String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = data[0];
        int next = 1;
        int bitsLeft = 8;
        while (bitsLeft > 0 || next < data.length) {
            if (bitsLeft < 5) {
                if (next < data.length) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xFF);
                    bitsLeft += 8;
                } else {
                    int pad = 5 - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = (buffer >> (bitsLeft - 5)) & 0x1F;
            sb.append(BASE32_CHARS.charAt(index));
            bitsLeft -= 5;
        }
        return sb.toString();
    }

    private byte[] decodeBase32(String base32) {
        base32 = base32.toUpperCase().replaceAll("[^" + BASE32_CHARS + "]", "");
        byte[] bytes = new byte[base32.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int next = 0;
        for (char c : base32.toCharArray()) {
            buffer <<= 5;
            buffer |= BASE32_CHARS.indexOf(c);
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[next++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return bytes;
    }

    /**
     * Validates a TOTP code against a secret.
     */
    public boolean verifyCode(String secret, String code) {
        if (code == null || code.length() != CODE_DIGITS) return false;
        
        try {
            long currentInterval = System.currentTimeMillis() / 1000 / TIME_STEP;
            // Check current, previous, and next intervals to handle clock drift (±90s)
            for (int i = -3; i <= 3; i++) {
                if (calculateCode(secret, currentInterval + i).equals(code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Generates an otpauth URI for QR codes.
     */
    public String getOtpAuthUri(String secret, String email) {
        String issuer = "WellCare Connect";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer.replace(" ", "%20"),
                email,
                secret,
                issuer.replace(" ", "%20"));
    }

    /**
     * Generates backup codes.
     */
    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < count; i++) {
            int code = 10000000 + random.nextInt(90000000); // 8 digits
            codes.add(String.valueOf(code));
        }
        return codes;
    }

    private String calculateCode(String secret, long interval) throws GeneralSecurityException {
        byte[] key = decodeBase32(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(interval).array();

        SecretKeySpec signKey = new SecretKeySpec(key, CRYPTO_ALGO);
        Mac mac = Mac.getInstance(CRYPTO_ALGO);
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xf;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xff);
        }

        truncatedHash &= 0x7fffffff;
        truncatedHash %= Math.pow(10, CODE_DIGITS);

        return String.format("%0" + CODE_DIGITS + "d", (int) truncatedHash);
    }
}
