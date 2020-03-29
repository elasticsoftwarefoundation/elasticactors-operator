#!/usr/bin/env bash

# make sure we are in the correct workspace (elasticactors)
kubectl config set-context --current --namespace=elasticactors
# create rbac rules and crds
kubectl apply -f deploy/
# now package and deploy
QUARKUS_OPTIONS="-Dquarkus.container-image.build=true -Dquarkus.container-image.push=true -Dquarkus.container-image.group=elasticactors -Dquarkus.container-image.registry=docker.io -Dquarkus.kubernetes.image-pull-policy=Always -Dquarkus.native.native-image-xmx=10G -Dquarkus.kubernetes.deploy=true"

# a profile can be input: build.sh native
if [[ -n "${1}" && "native" == "${1}" ]]; then
./mvnw -s ../settings.xml -Dmaven.test.skip=true -P${1} package ${QUARKUS_OPTIONS} -Dquarkus.kubernetes.mounts.elasticactors-operator-config.path=/work/config
else
./mvnw -s ../settings.xml -Dmaven.test.skip=true package ${QUARKUS_OPTIONS} -Dquarkus.kubernetes.mounts.elasticactors-operator-config.path=/config
fi

