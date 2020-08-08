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
