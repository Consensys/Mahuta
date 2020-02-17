Mahuta
======

**Mahuta** (formerly known as IPFS-Store) is a library to aggregate and consolidate files or documents stored by your application on the IPFS network. It provides a solution to collect, store, index, cache and search IPFS data handled by your system in a convenient way.

## Project status

| Service | Master | Development |
| -------- | -------- | -------- |
| CI Status | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/master.svg) | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/development.svg) |
| Test Coverage | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=master)](https://coveralls.io/github/ConsenSys/Mahuta?branch=master) | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=development)](https://coveralls.io/github/ConsenSys/Mahuta?branch=development) |
| Bintray | [ ![Bintray](https://api.bintray.com/packages/consensys/kauri/mahuta/images/download.svg) ](https://bintray.com/consensys/kauri/mahuta/_latestVersion) |
| Docker | [![](https://img.shields.io/docker/pulls/gjeanmart/mahuta.svg?style=flat)](https://hub.docker.com/r/gjeanmart/mahuta)|
| Sonar | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gjeanmart_IPFS-Store&metric=alert_status)](https://sonarcloud.io/dashboard?id=gjeanmart_IPFS-Store) |



## Features

- **Indexation**: Mahuta stores documents or files on IPFS and index the hash with optional metadata.
- **Discovery**: Documents and files indexed can be searched using complex logical queries or fuzzy/full text search)
- **Scalable**: Optimised for large scale applications using asynchronous writing mechanism and caching
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

See how to run those two components first [run IPFS and ElasticSearch](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/run_ipfs_and_elasticsearch.md)

## Java library

1. Import the Maven dependencies (core module + indexer)

```xml
<repository>
    <id>consensys-kauri</id>
    <name>consensys-kauri</name>
    <url>https://consensys.bintray.com/kauri/</url>
</repository>
```    

````xml
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

For more info, [Mahuta Java API](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/mahuta_java_api.md)

## Spring-Data

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
@IPFSDocument(index = "article", indexConfiguration = "article_mapping.json", indexContent = true)
public class Article {
    
    @Id
    private String id;

    @Hash
    private String hash;

    @Fulltext
    private String title;

    @Fulltext
    private String content;

    @Indexfield
    private Date createdAt;

    @Indexfield
    private String createdBy;
}



public class ArticleRepository extends MahutaRepositoryImpl<Article, String> {

    public ArticleRepository(Mahuta mahuta) {
        super(mahuta);
    }
}
```


For more info, [Mahuta Spring Data](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/mahuta_spring_data.md)


## HTTP API with Docker

### Prerequisites

- Docker
- [run IPFS and ElasticSearch with Docker](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/run_ipfs_and_elasticsearch.md#Docker)


### Docker

```
$ docker run -it --name mahuta \ 
    -p 8040:8040 \
    -e MAHUTA_IPFS_HOST=ipfs \
    -e MAHUTA_ELASTICSEARCH_HOST=elasticsearch \
    gjeanmart/mahuta
```

### Docker Compose

Check out the [documentation](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/mahuta_docker-compose.md) to configure Mahuta HTTP-API with Docker.

### Examples

To access the API documentation, go to [Mahuta HTTP API](https://github.com/ConsenSys/Mahuta/blob/master/mahuta-docs/mahuta_http-api.md)

#### Create the index `article` 

-   *Sample Request:*

```
curl -X POST \
  http://localhost:8040/mahuta/config/index/article \
  -H 'Content-Type: application/json' 
```

-   *Success Response:*

    -   Code: 200  
        Content:
```
{
    "status": "SUCCESS"
}
```

#### Store and index an article and its metadata

-   *Sample Request:*

```
curl -X POST \
  'http://localhost:8040/mahuta/index' \
  -H 'content-type: application/json' \
  -d '{"content":"# Hello world,\n this is my first file stored on **IPFS**","indexName":"article","indexDocId":"hello_world","contentType":"text/markdown","index_fields":{"title":"Hello world","author":"Gregoire Jeanmart","votes":10,"date_created":1518700549,"tags":["general"]}}'
```

-   *Success Response:*

    -   Code: 200  
        Content:
```
{
  "indexName": "article",
  "indexDocId": "hello_world",
  "contentId": "QmWHR4e1JHMs2h7XtbDsS9r2oQkyuzVr5bHdkEMYiqfeNm",
  "contentType": "text/markdown",
  "content": null,
  "pinned": true,
  "indexFields": {
    "title": "Hello world",
    "author": "Gregoire Jeanmart",
    "votes": 10,
    "createAt": 1518700549,
    "tags": [
      "general"
    ]
  },
  "status": "SUCCESS"
}
```

#### Search by query


-   *Sample Request:*

```
curl -X POST \
 'http://localhost:8040/mahuta/query/search?index=article' \
 -H 'content-type: application/json' \
 -d '{"query":[{"name":"title","operation":"CONTAINS","value":"Hello"},{"name":"author.keyword","operation":"EQUALS","value":"Gregoire Jeanmart"},{"name":"votes","operation":"GT","value":"5"}]}'
```

-   *Success Response:*

    -   Code: 200  
        Content:

```
{
  "status": "SUCCESS",
  "page": {
    "pageRequest": {
      "page": 0,
      "size": 20,
      "sort": null,
      "direction": "ASC"
    },
    "elements": [
      {
        "metadata": {
          "indexName": "article",
          "indexDocId": "hello_world",
          "contentId": "Qmd6VkHiLbLPncVQiewQe3SBP8rrG96HTkYkLbMzMe6tP2",
          "contentType": "text/markdown",
          "content": null,
          "pinned": true,
          "indexFields": {
            "author": "Gregoire Jeanmart",
            "votes": 10,
            "title": "Hello world",
            "createAt": 1518700549,
            "tags": [
              "general"
            ]
          }
        },
        "payload": null
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```
