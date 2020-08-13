package com.github.brunocrt.keycloak.custompin.generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CustomOTPGeneratorTest {

    private static final Logger LOGGER = Logger.getLogger(CustomOTPGeneratorTest.class.getName());

    private static final boolean LOG_ENABLED = true; // if you want to speed up testing disable this

    CustomOTPGenerator generator;

    private static final String SEED = "123";
    private static final String DATA = "abc";

    @Before
    public void setUp() throws Exception {
        generator = new CustomOTPGenerator("HH:mm:ss", ChronoUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        generator = null;
    }

    @Test
    public void basicPINGeneration() throws PINGeneratorException {
        String pin = generator.generate(SEED,DATA);

        log("PIN generated: "+pin);

        assertNotNull(pin);
        assertTrue(pin.length() > 6);
    }

    @Test(expected = PINGeneratorException.class)
    public void nullSeedException() throws PINGeneratorException {
        generator.generate(null,DATA);
    }

    @Test(expected = PINGeneratorException.class)
    public void nullDataException() throws PINGeneratorException {
        generator.generate(SEED,null);
    }

    @Test
    public void sequenceOf10EqualPIN() throws PINGeneratorException, InterruptedException {
        List<String> pinList = new ArrayList<String>();
        for(int i=0; i<11; i++) {
            String pin = generator.generate(SEED,DATA);
            log(" - PIN generated["+String.valueOf(i)+"]: "+pin);
            pinList.add(pin);
        }
        pinList.remove(0); // discard the first pin
        String previousPIN = pinList.remove(0);
        for(String pin : pinList) {
            Assert.assertEquals(pin, previousPIN);
            previousPIN = pin;
        }
    }

    @Test
    public void sequenceOf3DifferentPIN() throws PINGeneratorException, InterruptedException {
        List<String> pinList = new ArrayList<String>();
        for(int i=0; i<10; i++) {
            String pin = generator.generate(SEED,DATA);
            log(" - PIN generated["+String.valueOf(i)+"]: "+pin);
            pinList.add(pin);
            Thread.sleep(2000 * 1); // wait 1 sec before next generation
        }
        String previousPIN = pinList.remove(0);
        for(String pin : pinList) {
            Assert.assertNotEquals(pin, previousPIN);
            previousPIN = pin;
        }
    }

    @Test
    public void sequenceOf100KWithoutCollision() throws PINGeneratorException {
        Map<String,String> pinList = new HashMap<String,String>();
        for(int i=0; i<100000; i++) {
            String userId = generateRandomUserId();
            String pin = generator.generate(SEED, userId);
            pinList.put(userId, pin);
        }

        log("Total of PINs generated: "+pinList.keySet().size());

        String first = pinList.keySet().stream().findFirst().get();
        log("First: "+first+" - "+pinList.get(first));

        String previousPIN = pinList.remove(first);
        for(String pin : pinList.keySet()) {
            Assert.assertNotEquals(pin, previousPIN);
            previousPIN = pin;
        }
    }

    @Test
    public void testUserIdGeneration() {
        String userId = generateRandomUserId();
        Assert.assertNotNull(userId);
        Assert.assertEquals(userId.length(), 11);
        log(" - User generated:"+userId);
    }


    /**
     * Generate a random user id composed of 11 digits
     * @return
     */
    public String generateRandomUserId() {
        int n = 9;

        int d1=0 , d2 = 0;
        int d1factor1 = 10;
        int d2factor2 = 11;

        int[] numbers = new int[9];

        for(int i=0; i<9; i++) {
            numbers[i] = (int) (Math.random() * n);
            d1 += numbers[i] * (d1factor1);
            d2 += numbers[i] * (d2factor2);
            d1factor1--;
            d2factor2--;
        }

        d2 = d1 * 2 + d2;

        d1 = 11 - (mod(d1, 11));

        if (d1 >= 10)
            d1 = 0;

        d2 = 11 - (mod(d2, 11));

        if (d2 >= 10)
            d2 = 0;

       StringBuilder result = new StringBuilder();

       for(int x=0;x<numbers.length;x++)
           result.append(String.valueOf(numbers[x]));

       result.append(String.valueOf(d1)).append(String.valueOf(d2));

        return result.toString();
    }

    private int mod(int n1, int n2) {
        return (int) Math.round(n1 - (Math.floor(n1 / n2) * n2));
    }

    public void log(String message) {
        if(LOG_ENABLED)
            LOGGER.info(message);
    }

}