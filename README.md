IPFS-Store
======

**IPFS-Store** is an easy to use API on top of IPFS with the following features:
- IPFS proxy
- Search capabilities
- Pinning strategy (replication stretegy)

[![IPFS-_Store_-_New_frame.jpg](https://s22.postimg.cc/pgubsxo7l/IPFS-_Store_-_New_frame.jpg)](https://postimg.cc/image/mziklo4b1/)

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

$ java -jar target/ipfs-store-exec.jar
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


#### Configuration

Represents the configuration operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| create_index | Create an index in ElasticSearch |POST | /ipfs-store/config/index/{index} |

[WIKI: API-Documentation](https://github.com/ConsenSys/IPFS-Store/wiki/3.-API-Documentation)




## Advanced Configuration

[WIKI: Configuration](https://github.com/ConsenSys/IPFS-Store/wiki/2.-Configuration)



## Clients

[WIKI: Clients](https://github.com/ConsenSys/IPFS-Store/wiki/5.-Clients)



## Examples

[WIKI: Examples](https://github.com/ConsenSys/IPFS-Store/wiki/6.-Examples)
