#!/bin/bash


echo "removing old containers"
docker rm mahuta-service_ipfs_1
docker rm mahuta-service_elasticsearch_1
docker rm mahuta-service_mahuta_1


echo "removing storages"
sudo rm -rf .elasticsearch-docker
sudo rm -rf .ipfs-docker-data
sudo rm -rf .ipfs-docker-staging


echo "Build"
mvn clean install -f ../pom.xml
[ $? -eq 0 ] || exit $?; 

docker-compose build
[ $? -eq 0 ] || exit $?; 

echo "Start"
docker-compose up 
[ $? -eq 0 ] || exit $?; 

trap "docker-compose kill" INT
