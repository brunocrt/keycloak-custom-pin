package com.github.brunocrt.keycloak.custompin.generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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
        generator = new CustomOTPGenerator("HH:mm:ss");
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
            Thread.sleep(1000 * 1); // wait 1 sec before next generation
        }
        String previousPIN = pinList.remove(0);
        for(String pin : pinList) {
            Assert.assertNotEquals(pin, previousPIN);
            previousPIN = pin;
        }
    }

    public void log(String message) {
        if(LOG_ENABLED)
            LOGGER.info(message);
    }

}