#!/usr/bin/env bash

echo "Setting backend postgres database for CRUD API server in docker"
command -v docker >/dev/null 2>&1 || { echo >&2 "The setup requires docker but it's not installed.  Aborting."; exit 1; }

tag=latest

if [[ $# -eq 1 ]]
then
    tag = $1
fi;

echo "Pulling postgres docker image of tag: $tag"

mkdir -p $HOME/docker/volumes/postgres

docker run --rm --name pg-crudapi \
    -e POSTGRES_PASSWORD=docker \
    -p 5432:5432 \
    -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data \
    -d postgres:$tag

docker ps | grep pg-crudapi

docker exec -it pg-crudapi psql -U postgres -c "create database crudapi"

echo "$ psql -h localhost -U postgres -d crudapi"

echo "Password: docker"