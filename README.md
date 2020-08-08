# Keycloak Custom PIN Generator & Validator 

This is keycloak extension that creates a custom REST endpoint inside Keycloak. 
It can be used as an alternative (temporary) user authentication for JWT Token 
generation by a trusted server-side client application (using a service account) 
in behalf of the actual user without providing its password.

For testing this project locally in your machine you do need to have docker and 
docker compose installed otherwise you have to build the package and deploy it 
directly onto your development keycloak server.

## PIN Generation using custom endpoint
 - The custom endpoint generates a PIN (Custom user temporary code) through a new keycloak endpoint /pin (http://localhost:8080/auth/realms/example-realm/protocol/openid-connect/pin)

    In order to generate the PIN code you do need to register a client and request the PIN via the above URL passing the 'username' as a POST data parameter (already registred into keycloak), if the username is found then a unique PIN code is generated for this user and returned in the response body (REQUESTED_PIN=abc123!).

    The PIN is configured to generate a 1 minute valid code composed of letters, numbers and special characters of 8 digits length that uses HMAC-SHA algorithm to avoid collisions. It users the expiration time-window configuration and secret seed that must be defined by your
    project.

    Before you be able to access the pin endpoint you do need to authenticate your client. It must have being configured as
    a service account on keycloak. It also needs to have a keycloak realm created, in this example I have myclient registered into myrealm realm.
    
    Client Authentication
    
    `curl -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=myclient" -d "client_secret=c7ffe04d-5129-42b3-b3f2-ba9cc2a75bd9" -d "grant_type=client_credentials" http://localhost:8080/auth/realms/myrealm/protocol/openid-connect/token`

    In client authentication response you need to capture the "access_token" header value and provide it on
    the PIN request as the example bellow:

    PIN Request
    
    `curl -H "Authorization: Bearer eyJhbGc..." -d "username=myUser" http://localhost:8080/auth/realms/myrealm/pin`
    
    PIN Response
    
    `{REQUESTED_PIN: "7!DlaCrJ"}`


## JWT Token generation using PIN
- The PIN code then can be used to generate a valid Keykloak default JWT session token through the default /token endpoint without the need to provide the user password (http://localhost:8080/auth/realms/example-realm/protocol/openid-connect/token). You only need to provide the PIN code along with the user identification (username parameter) only. As a response keykloak will provide the usual JWT token with user details following the standard
keykloak configuration.

## Build
```bash
mvn clean package
```

## Test with docker-compose

The following scripts copy the generated package into keycloak server (/opt/jboss/keycloak/standalone/deployments/) and starts keycloak server
using docker-compose for testing it locally.


```bash
./start-keycloak.sh
```

```Windows cmd
start-keycloak.cmd
```

After start open the URL http://localhost:8080/auth
