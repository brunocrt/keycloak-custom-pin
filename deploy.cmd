@Echo off

mvn clean -Dmaven.test.skip=true package
call stop-keycloak
call start-keycloak
