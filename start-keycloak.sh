#!/bin/bash

set -e

VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)
PLUGIN_FILE="keycloak-custom-pin-$VERSION.jar"

cp target/$PLUGIN_FILE docker/
docker-compose up -d
sleep 60 # Waiting for Keycloak
docker-compose exec keycloak /opt/jboss/keycloak/bin/add-user-keycloak.sh -u admin -p admin
docker-compose exec keycloak cp -r /mnt/$PLUGIN_FILE /opt/jboss/keycloak/standalone/deployments/$PLUGIN_FILE
docker-compose restart keycloak
