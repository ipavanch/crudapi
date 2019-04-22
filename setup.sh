#!/usr/bin/env bash

# https://hackernoon.com/dont-install-postgres-docker-pull-postgres-bee20e200198

echo "Setting backend postgres database for CRUD API server in docker"
command -v docker >/dev/null 2>&1 || { echo >&2 "The setup requires docker but it's not installed.  Aborting."; exit 1; }

tag=latest

if [[ $# -eq 1 ]]
then
    tag = $1
fi;

echo "Pulling postgres docker image of tag: $tag"

docker run --rm --name pg-crudapi \
    -e POSTGRES_PASSWORD=docker \
    -p 5432:5432 \
    -d postgres:$tag

docker ps | grep pg-crudapi

sleep 20

docker exec pg-crudapi psql -U postgres -c "create database crudapi;"

docker exec pg-crudapi psql -U postgres -c "create user crudapi with encrypted password 'crudapi';"

docker exec pg-crudapi psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE crudapi TO crudapi;"


echo "$ psql -h localhost -U crudapi -d crudapi"

echo "Password: crudapi_admin"