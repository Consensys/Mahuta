Mahuta
======

**Mahuta** (formerly known as IPFS-Store) is a convenient library to aggregate and consolidate files or documents stored by your application on the IPFS network. It provides a solution to collect, store, index and search data used.

## Project status

| Service | Master | Development |
| -------- | -------- | -------- |
| CI Status | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/master.svg) | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/development.svg) |
| Test Coverage | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=master)](https://coveralls.io/github/ConsenSys/Mahuta?branch=master) | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=development)](https://coveralls.io/github/ConsenSys/Mahuta?branch=development) |
| Bintray | [ ![Bintray](https://api.bintray.com/packages/consensys/kauri/mahuta/images/download.svg) ](https://bintray.com/consensys/kauri/mahuta/_latestVersion) |
| Docker | [![](https://img.shields.io/docker/pulls/gjeanmart/mahuta.svg?style=flat)](https://cloud.docker.com/repository/docker/gjeanmart/mahuta)|
| Sonar | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gjeanmart_IPFS-Store&metric=alert_status)](https://sonarcloud.io/dashboard?id=gjeanmart_IPFS-Store) |


## Features

- **Indexation**: Mahuta stores documents or files on IPFS and index the hash with optional metadata.
- **Discovery**: Documents and files indexed can be searched using complex logical queries or fuzzy/full text search)
- **Scalable**: Optimised for large scale applications using asynchronous writing mechanism
- **Replication**: Replica set can be configured to replicate (pin) content across multiple nodes (standard IPFS node or IPFS-cluster node)
- **Multi-platform**: Mahuta can be used as a simple embedded Java library for your JVM-based application or run as a simple, scalable and configurable Rest API.


![Mahuta.jpg](https://imgur.com/tIdQRD8.png)


-------------------------------------------------------------------------


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

#### Prerequisites

Mahuta depends of two components:
- an IPFS node ([go](https://github.com/ipfs/go-ipfs) or [js](https://github.com/ipfs/js-ipfs) implementation)
- a search engine (currently only ElasticSearch is supported)

You will need to run those two components first, see [run IPFS and ElasticSearch](mahuta-docs/run_ipfs_and_elasticsearch.md)

### Java library

1. Import the Maven dependencies (core module + indexer)


````
<dependency>
    <groupId>net.consensys.mahuta</groupId>
    <artifactId>mahuta-core</artifactId>
    <version>${MAHUTA_VERSION}</version>
</dependency>
<dependency>
    <groupId>net.consensys.mahuta</groupId>
    <artifactId>mahuta-indexing-elasticsearch</artifactId>
    <version>${MAHUTA_VERSION}</version>
</dependency>
````

2. Configure Mahuta to connect to an IPFS node and an indexer

```
Mahuta mahuta = new MahutaFactory()
    .configureStorage(IPFSService.connect("localhost", 5001))
    .configureIndexer(ElasticSearchService.connect("localhost", 9300, "cluster-name"))
    .defaultImplementation();
```

3. Execute high-level operations

```
IndexingResponse response = mahuta.prepareStringIndexing("article", "## This is my first article")
    .contentType("text/markdown")
    .indexDocId("article-1")
    .indexFields(ImmutableMap.of("title", "First Article", "author", "greg"))
    .execute();
    
GetResponse response = mahuta.prepareGet()
    .indexName("article")
    .indexDocId("article-1")
    .loadFile(true)
    .execute();
    
SearchResponse response = mahuta.prepareSearch()
    .indexName("article")
    .query(Query.newQuery().equals("author", "greg"))
    .pageRequest(PageRequest.of(0, 20))
    .execute();
```

For more info, [Mahuta Java API](mahuta-docs/mahuta_java_api.md)

### Spring-Data

1. Import the Maven dependencies 

````
<dependency>
    <groupId>net.consensys.mahuta</groupId>
    <artifactId>mahuta-springdata</artifactId>
    <version>${MAHUTA_VERSION}</version>
</dependency>
````

2. Configure your spring-data repository

```
public class ArticleRepository extends MahutaRepositoryImpl<Article, String> {

    public ArticleRepository(Mahuta mahuta) {
        super(mahuta, "article", Sets.newSet("title", "author"), Sets.newSet("title"), Article.class);
    }
}
```


For more info, [Mahuta Spring Data](mahuta-docs/mahuta_spring_data.md)


### HTTP API

#### From source

##### Prerequisites

- Java 8
- Maven

##### Steps

1. After checking out the code, navigate to the root directory

```
$ cd /path/to/mahuta/mahuta-http-api/
```

2. Compile, test and package the project

```
$ mvn clean package
```

3. Configure environment variables

```
$ export MAHUTA_IPFS_HOST=localhost
$ export MAHUTA_IPFS_PORT=5001
$ export MAHUTA_ELASTICSEARCH_HOST=localhost
$ export MAHUTA_ELASTICSEARCH_PORT=9300
$ export MAHUTA_ELASTICSEARCH_CLUSTERNAME=cluster_name
```


4. Run the service

```
$ java -jar target/mahuta-http-api-exec.jar
```

#### Docker


##### Prerequisites

- Docker
- [run IPFS and ElasticSearch with Docker](mahuta-docs/run_ipfs_and_elasticsearch.md#Docker)


##### Steps

```
$ docker run -it --name mahuta \ 
    -p 8040:8040 \
    -e MAHUTA_IPFS_HOST=ipfs \
    -e MAHUTA_ELASTICSEARCH_HOST=elasticsearch \
    gjeanmart/mahuta
```

##### docker-compose

[Docker-compose](mahuta-docs/mahuta_docker-compose.md)


#### API samples

For the full documentation including configuration and details of each operation: [Mahuta HTTP API](mahuta-docs/mahuta_http-api.md)

##### Store and index 

-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/index' \
    -H 'content-type: application/json' \  
    -d '{"content":"# Hello world,\n this is my first file stored on **IPFS**","indexName":"articles","indexDocId":"hello_world","contentType":"text/markdown","index_fields":{"title":"Hello world","author":"Gregoire Jeanmart","votes":10,"date_created":1518700549,"tags":["general"]}}'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**
```
{
    "status": "SUCCESS",
    "indexName": "articles",
    "id": "hello_world",
    "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o"
}

##### Search 


-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/query/search?index=articles' \
    -H 'content-type: application/json' \  
    -d '{"query":[{"name":"title","operation":"CONTAINS","value":"Hello"},{"name":"author","operation":"EQUALS","value":"Gregoire Jeanmart"},{"name":"votes","operation":"LT","value":"5"}]}'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**

```
{
  "elements": [
    {
        "metadata": {
          "indexName": "articles",
          "indexDocId": "hello_world",
          "contentId": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o",
          "contentType": "application/pdf",
          "indexFields": {
              "title": "Hello world",
              "description": "Hello world this is my first file stored on IPFS",
              "author": "Gregoire Jeanmart",
              "votes": 10,
              "date_created": 1518700549,
              "tags": ["general"]
          }
        },
        "payload": "# Hello world,\n this is my first file stored on **IPFS**"
    }
  ]
}
],,
"totalElements": 4,
"totalPages": 1
}
```


