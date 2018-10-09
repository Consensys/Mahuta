#!/bin/bash


echo "removing old containers"
docker rm ipfsstoreservice_ipfs0_1
docker rm ipfsstoreservice_ipfs1_1
docker rm ipfsstoreservice_ipfs-cluster0_1
docker rm ipfsstoreservice_ipfs-cluster1_1
docker rm ipfsstoreservice_elasticsearch_1
docker rm ipfsstoreservice_ipfs-store_1


echo "removing storages"
sudo rm -rf .elasticsearch-docker
sudo rm -rf .ipfs0-docker-data
sudo rm -rf .ipfs0-docker-staging
sudo rm -rf .ipfs1-docker-data
sudo rm -rf .ipfs1-docker-staging
sudo rm -rf .ipfs-cluster0
sudo rm -rf .ipfs-cluster1


echo "Build"
mvn clean install -f ../pom.xml
[ $? -eq 0 ] || exit $?;

docker-compose -f docker-compose-ipfscluster.yml build
[ $? -eq 0 ] || exit $?;

echo "Start"
docker-compose -f docker-compose-ipfscluster.yml up
[ $? -eq 0 ] || exit $?;

trap "docker-compose kill" INT
