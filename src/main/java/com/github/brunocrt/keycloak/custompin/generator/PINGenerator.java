package com.github.brunocrt.keycloak.custompin.generator;

public interface PINGenerator {

    public String generate(String seed, String data) throws PINGeneratorException;
}
