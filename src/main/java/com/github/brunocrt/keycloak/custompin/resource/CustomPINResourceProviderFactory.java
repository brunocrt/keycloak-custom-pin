package com.github.brunocrt.keycloak.custompin.resource;

import com.github.brunocrt.keycloak.custompin.generator.CustomOTPGenerator;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * This class creates custom REST endpoint on KeyCloak for generate a time-based
 * (OTP) PIN code using KeyCloak Resource Provider extension.
 *
 * This class produces on keycloak a custom endpoint accessible via the follow URL
 * http://localhost:8080/auth/realms/myrealm/pin
 *
 * @author brunocrt
 * @date 07/Aug/2020
 */
public class CustomPINResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "pin";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new CustomPINResourceProvider(session, new CustomOTPGenerator());
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}