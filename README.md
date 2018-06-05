IPFS-Store
======

**IPFS-Store** aim is to provide an easy to use IPFS storage service with search capability for your project.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

- Java 8
- Maven 
- Docker (optional)


### Build

1. After checking out the code, navigate to the root directory
```
$ cd /path/to/ipfs-store/ipfs-store-service/
```

2. Compile, test and package the project
```
$ mvn clean package
```

3. Run the project

a. If you have a running instance of IPFS and ElasticSearch 

**Executable JAR:**

```
$ export IPFS_HOST=localhost
$ export IPFS_PORT=5001
$ export ELASTIC_HOST=localhost
$ export ELASTIC_PORT=9300
$ export ELASTIC_CLUSTERNAME=elasticsearch

$ java -jar target/ipfs-store.jar
```

**Docker:**

```
$ docker build  . -t kauri/ipfs-store:latest

$ export IPFS_HOST=localhost
$ export IPFS_PORT=5001
$ export ELASTIC_HOST=localhost
$ export ELASTIC_PORT=9300
$ export ELASTIC_CLUSTERNAME=elasticsearch

$ docker run -p 8040:8040 kauri/ipfs-store
```

b. If you prefer build all-in-one with docker-compose
```
$ docker-compose -f docker-compose.yml build
$ docker-compose -f docker-compose.yml up
```

[WIKI: Getting-started](https://github.com/ConsenSys/IPFS-Store/wiki/1.-Getting-started)


## API Documentation

### Overview

#### Configuration

Represents the configuration operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| create_index | Create an index in ElasticSearch |POST | /ipfs-store/config/index/{index} |

##### Persistence

Represents the writting operations.

###### Raw

Enable to store any kind of content. The API uses HTTP multipart to sent the data or file over the request.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| store | Store raw content into IPFS |POST | /ipfs-store/raw/store |
| index | Indexraw content |POST | /ipfs-store/raw/index |
| store_index | Store & Index raw content | POST | /ipfs-store/raw/store_index |

###### JSON

Enable to store JSON document. 

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| store | Store json content into IPFS |POST | /ipfs-store/json/store |
| index | Index json content |POST | /ipfs-store/json/index |
| store_index | Store & Index json content | POST | /ipfs-store/json/store_index |

##### Query
Represents the read operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| fetch | Get content | GET | /ipfs-store/query/fetch/{hash} |
| search | Search content | POST | /ipfs-store/query/search |
| search | Search content | GET | /ipfs-store/query/search |



[WIKI: API-Documentation](https://github.com/ConsenSys/IPFS-Store/wiki/3.-API-Documentation)




## Advanced Configuration

The following section shows how to tweak IPFS-store. Any of these properties can be overwritten using command arguments ``--{property_name}={property_value}`` (for example `--server.port=8888`)


**Full configuration file:**

```
server:
  port: 8040
  contextPath: /ipfs-store

logging:
  level:
    net.consensys: ${LOG_LEVEL:TRACE}

ipfs-store:
  storage:
    type: IPFS
    host: ${IPFS_HOST:localhost}
    port: ${IPFS_PORT:5001}
    additional:
      timeout: 5000
      thread_pool: 10
    
  index:
    type: ELASTICSEARCH
    host: ${ELASTIC_HOST:localhost}
    port: ${ELASTIC_PORT:9300}
    additional:
      clusterName: ${ELASTIC_CLUSTERNAME:docker-cluster}
      indexNullValue: true
      
  pinning:
    strategies:
      - 
        id: ipfs_node
        type: native
        host: ${IPFS_HOST:localhost}
        port: ${IPFS_PORT:5001}
      - 
        id: ipfs_cluster
        type: ipfs_cluster
        enable: ${IPFS_CLUSTER_ENABLE:false}
        host: ${IPFS_CLUSTER_HOST:localhost}
        port: ${IPFS_CLUSTER_PORT:9094}

  api-spec:
    query:
      fetch: /query/fetch/{hash}
      search: /query/search
    config:
      index: /config/index/{index}
    persistence:
      raw:
        store: /raw/store
        index: /raw/index
        store_index: /raw/store_index
      json:
        store: /json/store
        index: /json/index
        store_index: /json/store_index
        
```


### Storage layer

The storage layer is built in a generic way where different storage technologies could be used.  

*At the moment, only IPFS is supported.*

#### IPFS

| Property | Type | Sample value | Description |
| -------- | -------- | -------- | -------- | 
| ipfs-store.storage.type | String | IPFS | Select IPFS as a storage layer |
| ipfs-store.storage.host | String | localhost | Host to connect to the node |
| ipfs-store.storage.port | Integer | 5000 | Port to connect to the node |
| ipfs-store.storage.additional.timeout | Integer | 10000 | Timeout to find a file by hash |



#### SWARM

*Coming soon*



### Index layer

The Index layer is built in a generic way where different search engine technologies could be used.  

*At the moment, only ElasticSearch is supported.*

#### ELASTICSEARCH

| Property | Type | Sample value | Description |
| -------- | -------- | -------- | -------- | 
| ipfs-store.index.type | String | ELASTICTSEARCH | Select ELASTICTSEARCH as a index layer |
| ipfs-store.index.host | String | localhost | Host to connect to ElasticSearch
| ipfs-store.index.port | Integer | 9300 | Port to connect to ElasticSearch |
| ipfs-store.index.additional.clusterName | String | es-cluster | Name of the cluster |
| ipfs-store.index.additional.indexNullValue | Boolean | true | Index empty/null value with a default value `NULL` (useful to search on empty field) |



### Pinning strategy

A pinning strategy define the way you want to pin (permanently store) your content. Whilst a `native` pinning strategy consists in pinning the content directly in a node. Another one, `ipfs_cluster` consist in pinning an IPFS-cluster to replicate the content across the cluster of nodes. Other implementations could be imagined.

Pinning stategies can be combined together and are executed asynchronously from the main thread.

**Strategies**
| Name (type) |Description |
| -------- | -------- | 
| native | Pin the node |
| ipfs_cluster | Pin an ipfs cluster node that replicates the content |


| Property | Type | Sample value | Description |
| -------- | -------- | -------- | -------- | 
| ipfs-store.pinning.strategies | Strategy[] | List og strategies|
| ipfs-store.pinning.strategies[0].id | String | Unique identifier of the stratefy |
| ipfs-store.pinning.strategies[0].type | String | Type of the strategy (`native`, `ipfs_cluster`) |
| ipfs-store.pinning.strategies[0].enable | Boolean | Enable/Disable the strategy |
| ipfs-store.pinning.strategies[0].host | String | Basic configuration (host)  
| ipfs-store.pinning.strategies[0].port | String | Basic configuration (port) | 
| ipfs-store.pinning.strategies[0].additional | Map | Basic configuration (additional) |


[WIKI: Configuration](https://github.com/ConsenSys/IPFS-Store/wiki/2.-Configuration)



## Clients

[WIKI: Clients](https://github.com/ConsenSys/IPFS-Store/wiki/4.-Clients)



## Examples

[WIKI: Examples](https://github.com/ConsenSys/IPFS-Store/wiki/5.-Examples)
