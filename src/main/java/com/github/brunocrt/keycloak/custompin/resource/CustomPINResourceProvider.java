package com.github.brunocrt.keycloak.custompin.resource;

import com.github.brunocrt.keycloak.custompin.generator.PINGenerator;
import com.github.brunocrt.keycloak.custompin.generator.PINGeneratorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

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

    private static final Logger LOGGER = Logger.getLogger(CustomPINResourceProvider.class.getName());

    private KeycloakSession session;
    private PINGenerator pinGenerator;

    private static final String INPUT_PARAM_USERNAME = "username";
    private static final String OUTPUT_PARAM_REALM = "REALM";
    private static final String OUTPUT_PARAM_PIN = "REQUESTED_PIN";

    private static final String GENERATOR_SEED = "1020304050";

    public CustomPINResourceProvider(KeycloakSession session, PINGenerator pinGenerator) {
        this.session = session;
        this.pinGenerator = pinGenerator;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@FormParam(INPUT_PARAM_USERNAME) String userName) {

        validateSession(this.session);

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

    @Override
    public void close() {
        // Not implemented
    }

    private void validateSession(KeycloakSession session) throws NotAuthorizedException {

        AccessToken accessToken = validateToken(session);

        if (accessToken == null) {
            LOGGER.warning("client not authenticated");
            throw new NotAuthorizedException("not_authenticated");
        }

        if (accessToken.getRealmAccess() == null) {
            LOGGER.warning("no realm associated with authorization");
            throw new NotAuthorizedException("no realm authorization");
        }

        if (!accessToken.isActive()) {
            LOGGER.warning("Token not active");
            throw new NotAuthorizedException("token not active");
        }

    }

    private AccessToken validateToken(KeycloakSession session) throws NotAuthorizedException {
        try {
            RealmModel realm = session.getContext().getRealm();
            HttpHeaders headers = session.getContext().getRequestHeaders();
            String tokenString = readAccessTokenFrom(headers);
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(tokenString, AccessToken.class).parse();
            AccessToken accessToken = verifier.getToken();
            // TODO: verify if token is valid, not only format nor expired (realm/client)
            return accessToken;
        } catch (NotAuthorizedException e) {
            throw e;
        } catch (VerificationException e) {
            LOGGER.warning("introspection of token failed: "+e.getMessage());
            throw new NotAuthorizedException("access_token_introspection_failed: "+e.getMessage());
        }
    }


    private String readAccessTokenFrom(HttpHeaders headers) throws NotAuthorizedException {
        String authorization = headers.getHeaderString(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            LOGGER.warning("no authorization header with bearer token");
            throw new NotAuthorizedException("bearer_token_missing_in_authorization_header");
        }
        String token = authorization.substring(7);
        if (token == null || token.isEmpty()) {
            LOGGER.warning("empty access token");
            throw new NotAuthorizedException("missing_access_token");
        }
        return token;
    }
}