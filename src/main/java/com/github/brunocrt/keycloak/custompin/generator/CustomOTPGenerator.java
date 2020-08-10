package com.github.brunocrt.keycloak.custompin.generator;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

public class CustomOTPGenerator implements PINGenerator {

    private final String HASH_ALGO = "HmacSHA256";
    private final String DEFAULT_TIME_FORMAT = "HH:mm";

    private String timeFormat;

    public CustomOTPGenerator() {
        this.timeFormat = DEFAULT_TIME_FORMAT;
    }

    public CustomOTPGenerator(String timeFormat) {
        this.timeFormat = timeFormat;
    }


    @Override
    public String generate(String seed, String data) throws PINGeneratorException {
        return enforcedHMAC(seed, data, 8);
    }

    /**
     * This generates a modified version of HMAC enforcing special characters.
     * @param seed password seed (salt)
     * @param data original data to be encrypted
     * @param length length of the result code
     * @return 8 characters message code
     */
    public String enforcedHMAC(String seed, String data, int length) throws PINGeneratorException {
        final String originalHMAC = generateHMAC(seed, data);
        for(int i=0; i<length; i++) {
            if(!Character.isDigit(originalHMAC.charAt(i)) &&
                    !Character.isLetter(originalHMAC.charAt(i))) {
                if(i > length) {
                    return originalHMAC.substring(0, length - 1) + originalHMAC.charAt(i);
                } else {
                    return originalHMAC.substring(0, length);
                }
            }
        }

        return originalHMAC.substring(0, length - 1) + "!";
    }

    /**
     * Generates a reduced password according to a specified length parameter
     * @param seed password seed (salt)
     * @param data original data to be encrypted
     * @param length the length of the encrypted password result. Minimal 6 characters.
     * @return
     */
    public String truncatedHMAC(String seed, String data, int length) throws PINGeneratorException {
        String originalPIN = generateHMAC(seed, data);
        if(length > 3 || length < originalPIN.length()) {
            return originalPIN.substring(0, length);
        } else {
            return originalPIN;
        }
    }

    /**
     * Generates an One Time Based Password (OTP) using HMAC 256 (SHA1) Algorithm.
     * This combines the provided seed with a time sensitive timestamp with 1 minute
     * of duration to encrypt the provided data.
     *
     * @param seed salt
     * @param data data to be encoded
     * @return Base 64 encoded password
     * @throws PINGeneratorException
     */
    public String generateHMAC(String seed, String data) throws PINGeneratorException {
        if(seed == null || data == null)
            throw new PINGeneratorException("All input data is required to generate the PIN");

        String time = LocalTime.now()
                .plus(1, ChronoUnit.MINUTES)
                .format(DateTimeFormatter.ofPattern(this.timeFormat));

        String secret = seed + time;

        try {
            Mac mac = Mac.getInstance(HASH_ALGO);
            SecretKeySpec secret_key = new SecretKeySpec((secret).getBytes(), HASH_ALGO);
            mac.init(secret_key);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
            throw new PINGeneratorException(e.getMessage());
        }
    }

}
