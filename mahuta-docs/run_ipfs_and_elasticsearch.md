Run IPFS and ElasticSearch
======

The following wiki page explains how to run the two required components of Mahuta: IPFS and Elasticsearch

## Docker

1. Create a virtual network

Let's create a private Docker network to make our containers able to communicate together.

```
$ docker network create mahuta-network
```

2. Start IPFS daemon

Start an IPFS daemon to join the IPFS network and expose the port 4001 (peer2p networking) and 5001 (API)

```
$ docker run -d --name ipfs \
   -v /path/to/ipfs/export:/export  \
   -v /path/to/ipfs/data:/data/ipfs 
   -p :4001:4001 \
   -p :5001:5001 \ 
   --net mahuta-network \
   ipfs/go-ipfs:latest
```

3. Start ElasticSearch

Start ElasticSearch database used as a content indexer.

```
$ docker run -d --name elasticsearch \
    -v /path/to/elasticsearch:/data/elasticsearch \
    -p 9200:9200 \ 
    -p 9300:9300 \
    -e "discovery.type=single-node" \
    --net mahuta-network \
    docker.elastic.co/elasticsearch/elasticsearch:6.5.0
```
