package com.github.brunocrt.keycloak.custompin.resource;

import com.github.brunocrt.keycloak.custompin.generator.PINGenerator;
import com.github.brunocrt.keycloak.custompin.generator.PINGeneratorException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom REST Endpoint for generate a time-based
 * (OTP) PIN code using KeyCloak Resource Provider extension.
 *
 * This class receives an keycloak username information and returns a
 * time sensitive (1 min) password code generated using SHA256 hash algorithm
 * and the Keycloak realm information where this endpoint was requested.
 *
 * This class must be deployed along the Factory class, for more information
 * see https://www.keycloak.org/docs/latest/server_development/#_extensions
 *
 * @author brunocrt
 * @date 07/Aug/2020
 */
public class CustomPINResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;
    private PINGenerator pinGenerator;
    private final AuthenticationManager.AuthResult auth;

    private static final String INPUT_PARAM_USERNAME = "username";
    private static final String OUTPUT_PARAM_REALM = "REALM";
    private static final String OUTPUT_PARAM_PIN = "REQUESTED_PIN";

    private static final String GENERATOR_SEED = "1020304050";

    public CustomPINResourceProvider(KeycloakSession session, PINGenerator pinGenerator) {
        this.session = session;
        this.pinGenerator = pinGenerator;
        this.auth = new AppAuthManager().authenticateBearerToken(session, session.getContext().getRealm());
    }

    @Override
    public Object getResource() {
        return this;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@FormParam(INPUT_PARAM_USERNAME) String userName) {

        // TODO: check if client is authenticated

        if(userName == null || userName.trim().length() == 0) {
            return Response.serverError().entity(INPUT_PARAM_USERNAME+" cannot be blank").build();
        }

        // check the realm
        String realmName = session.getContext().getRealm().getDisplayName();
        if (realmName == null) {
            realmName = session.getContext().getRealm().getName();
        }

        // TODO: check if the user is valid

        // Generate new PIN for the user
        String generatedPIN = null;
        try {
            generatedPIN = pinGenerator.generate(GENERATOR_SEED, userName);
        } catch (PINGeneratorException e) {
            Response.serverError().entity(e.getMessage());
        }

        Map<String,String> responseEntity = new HashMap<String,String>();
        responseEntity.put(OUTPUT_PARAM_REALM, realmName);
        responseEntity.put(OUTPUT_PARAM_PIN, generatedPIN);

        return Response.ok(responseEntity, MediaType.APPLICATION_JSON).build();
    }


    private void checkAuthentication() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getRealmAccess() == null) {
            throw new ForbiddenException("Does not have realm access");
        }
    }

    @Override
    public void close() {
        // Not implemented
    }

}