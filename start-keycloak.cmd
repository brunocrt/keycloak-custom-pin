@Echo off


for /f %%i in ('"mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout"') do set VERSION=%%i

echo "checking project version..."
echo %VERSION%

set PLUGIN_FILE=keycloak-custom-pin-%VERSION%.jar

echo "checking plugin file..."
echo %PLUGIN_FILE%

echo "copying generated jar (%PLUGIN_FILE%) to docker folder..."
COPY target\%PLUGIN_FILE% docker\
echo "Starting Keycloak with docker-compose..."
docker-compose up -d
ECHO "Waiting for Keycloak start..."
TIMEOUT 10
ECHO "Deploying the %PLUGIN_FILE% plugin..."
docker-compose exec keycloak /opt/jboss/keycloak/bin/add-user-keycloak.sh -u admin -p admin
docker-compose exec keycloak cp -r /mnt/%PLUGIN_FILE% /opt/jboss/keycloak/standalone/deployments/%PLUGIN_FILE%
docker-compose restart keycloak
ECHO "Waiting for Keycloak restart to apply changes..."
TIMEOUT 10
