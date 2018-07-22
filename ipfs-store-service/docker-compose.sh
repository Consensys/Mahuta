#!/bin/bash


echo "removing old containers"
docker rm ipfs-store-service_ipfs_1
docker rm ipfs-store-service_elasticsearch_1
docker rm ipfs-store-service_ipfs-store_1


echo "removing storages"
sudo rm -rf .elasticsearch-docker
sudo rm -rf .ipfs-docker-data
sudo rm -rf .ipfs-docker-staging


echo "Build"
mvn clean install -f ../pom.xml
docker-compose build


echo "Start"
docker-compose up 


trap "docker-compose kill" INT
