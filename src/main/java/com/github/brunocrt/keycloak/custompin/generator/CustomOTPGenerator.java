package com.github.brunocrt.keycloak.custompin.generator;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.logging.Logger;

public class CustomOTPGenerator implements PINGenerator {

    private static final Logger LOGGER = Logger.getLogger(CustomOTPGenerator.class.getName());

    // Algorithm config
    private final String        HASH_ALGO           = "HmacSHA256";

    // Time configuration
    private final ChronoUnit    DEFAULT_TIME_UNIT   = ChronoUnit.MINUTES; // minutes
    private final String        DEFAULT_TIME_FORMAT = "HH:mm"; // hours + minutes
    private final Integer       EXPIRE_TIME_RATIO   = 1; // 1 minute duration
    private final Integer       EXPIRE_TIME_GRACE   = 1; // 1 minute tolerance

    // Token configuration
    private final Integer       DEFAULT_OTP_LENGTH  = 8;

    private String timeFormat;
    private ChronoUnit timeUnit;

    public CustomOTPGenerator() {
        this.timeFormat = DEFAULT_TIME_FORMAT;
        this.timeUnit = DEFAULT_TIME_UNIT;
    }

    public CustomOTPGenerator(String timeFormat, ChronoUnit timeUnit) {
        this.timeFormat = timeFormat;
        this.timeUnit = timeUnit;
    }

    @Override
    public String generate(String seed, String data) throws PINGeneratorException {
        return enforcedHMAC(seed, data, DEFAULT_OTP_LENGTH);
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
                .plus(EXPIRE_TIME_RATIO, this.timeUnit)
                .format(DateTimeFormatter.ofPattern(this.timeFormat));

        // adjust time for tolerance (grace) period (ex. 1 minute + 1)
        String adjustedTime = String.valueOf( Integer.parseInt(time.replaceAll(":",""))/(EXPIRE_TIME_GRACE+1) );

        //LOGGER.fine("adjustedTime: "+time);

        String secret = seed + adjustedTime;

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
