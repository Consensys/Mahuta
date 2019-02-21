Mahuta
======

| Service | Master | Development |
| -------- | -------- | -------- |
| CI Status | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/master.svg) | ![](https://img.shields.io/circleci/project/github/ConsenSys/Mahuta/development.svg) |
| Test Coverage | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=master)](https://coveralls.io/github/ConsenSys/Mahuta?branch=master) | [![Coverage](https://coveralls.io/repos/github/ConsenSys/Mahuta/badge.svg?branch=development)](https://coveralls.io/github/ConsenSys/Mahuta?branch=development) |
| Bintray | [ ![Bintray](https://api.bintray.com/packages/consensys/kauri/mahuta/images/download.svg) ](https://bintray.com/consensys/kauri/mahuta/_latestVersion) |
| Docker | [![](https://img.shields.io/docker/pulls/gjeanmart/mahuta.svg?style=flat)](https://cloud.docker.com/repository/docker/gjeanmart/mahuta)|
| Sonar | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gjeanmart_IPFS-Store&metric=alert_status)](https://sonarcloud.io/dashboard?id=gjeanmart_IPFS-Store) |


**Mahuta** (formerly known as IPFS-Store) is a adaptable search engine opensource tool aiming to collect, store and index data on the IPFS network. This is a convenient solution for any applications storing data on IPFS which require content discovery (complex queries or full text search with fuzziness).

It can be deployed as a simple embedded Java library for your Java application or a simple, scalable and configurable API.

This service offers functionnality to store content on IPFS, replicate it across multiple nodes (IPFS node or IPFS-cluster node) and index it locally using ElasticSearch or Lucene. 

[![Mahuta.jpg](https://i.ibb.co/MMnvVmm/Untitled-New-frame-1.jpg)](https://i.ibb.co/MMnvVmm/Untitled-New-frame-1.jpg)


-------------------------------------------------------------------------


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

#### Prerequisites

Mahuta depends of two components:
- an IPFS node ([go](https://github.com/ipfs/go-ipfs) or [js](https://github.com/ipfs/js-ipfs) implementation)
- a search engine (currently only ElasticSearch is supported)

You will need to run those two components first. [see wiki page - run IPFS and ElasticSearch]

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
        .build();
```

For the full documentation, [see wiki page - Mahuta Java API]

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
public class MyRepository extends MahutaRepositoryImpl<MyEntity, String> {

}
```


[see wiki page - Mahuta Spring-Data]


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

3. Configure the service

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
- [see wiki page - run IPFS and ElasticSearch] 


##### Steps

```
$ docker run -it --name mahuta \ 
    -p 8040:8040 \
    -e MAHUTA_IPFS_HOST=ipfs \
    -e MAHUTA_ELASTICSEARCH_HOST=elasticsearch \
    gjeanmart/mahuta
```

##### docker-compose

[see wiki page - docker-compose]


For the full documentation including configuration and details of each method , [see wiki page - Mahuta HTTP API]


-------------------------------------------------------------------------


## API Documentation

### Overview

##### Persistence

Represents the writting operations.


| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| index_simple | Stored and index a text content | POST | /index |
| index_cid | Pin and index a CID | POST | /index/cid |
| index_file  | Store and index a file via HTTP Multipart | POST | /index/file ||

##### Query
Represents the read operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| fetch | Get content | GET | /query/fetch/{hash} |
| search | Search content | POST | /query/search |

#### Delete

Represents the deletion operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| delete_by_id | Unpin and deindex a file by ID | DELETE | /delete/id/{id} |
| delete_by_hash | Unpin and deindex a file by Hash | DELETE | /delete/hash/{hash} |


#### Configuration

Represents the configuration operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| create_index | Create an index | POST | /config/index/{index} |
| get_indexes | Get all indexes | GET | /config/index |


### Details


#### [Persistence] Store and Index text

Store and Index a text

-   **URL** `/mahuta/index`
-   **Method:** `POST`
-   **Header:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |


-   **URL Params** `N/A`
-   **Data Params**

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| content | String | yes |  | Content  |
| indexName | String | yes |  | Index name |
| indexDocId | String | no |  | Identifier of the document in the index. id null, autogenerated |
| contentType | String | no |  | Content type (mimetype) |
| indexFields | Object | no |  | Metadata can used to query the document |


```
{
  "content": "# Hello world,\n this is my first file stored on **IPFS**",
  "indexName": "articles",
  "indexDocId": "hello_world",
  "contentType": "text/markdown",
  "indexFields": {
      "title": "Hello world",
      "description": "Hello world this is my first file stored on IPFS",
      "author": "Gregoire Jeanmart",
      "votes": 10,
      "date_created": 1518700549,
      "tags": ["general"]
  }
}
```

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
```

---------------------------

#### [Persistence] Store and Index CID

Store and Index a text

-   **URL** `/mahuta/index/cid`
-   **Method:** `POST`
-   **Header:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |


-   **URL Params** `N/A`
-   **Data Params**

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| cid | String | yes |  | Content Hash |
| indexName | String | yes |  | Index name |
| indexDocId | String | no |  | Identifier of the document in the index. id null, autogenerated |
| contentType | String | no |  | Content type (mimetype) |
| indexFields | Object | no |  | Metadata can used to query the document |


```
{
  "cid": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o",
  "indexName": "articles",
  "indexDocId": "hello_world",
  "contentType": "text/markdown",
  "indexFields": {
      "title": "Hello world",
      "description": "Hello world this is my first file stored on IPFS",
      "author": "Gregoire Jeanmart",
      "votes": 10,
      "date_created": 1518700549,
      "tags": ["general"]
  }
}
```

-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/index/cid' \
    -H 'content-type: application/json' \  
    -d '{"cid":"QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o","indexName":"articles","indexDocId":"hello_world","contentType":"text/markdown","index_fields":{"title":"Hello world","author":"Gregoire Jeanmart","votes":10,"date_created":1518700549,"tags":["general"]}}'
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
```

---------------------------

#### [Persistencd] Store and Index file

Store content in IPFS and index it into the search engine

-   **URL** `/mahuta/index/file`
-   **Method:** `POST`
-   **Header:** `N/A`
-   **URL Params** `N/A`
-   **Data Params**


    -   `file: [content]`
    -   `request: `

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| indexName | String | yes |  | Index name |
| indexDocId | String | no |  | Identifier of the document in the index. id null, autogenerated |
| contentType | String | no |  | Content type (mimetype) |
| indexFields | Object | no |  | Metadata can used to query the document |


```
{
  "indexName": "articles",
  "indexDocId": "hello_world",
  "contentType": "text/markdown",
  "indexFields": {
      "title": "Hello world",
      "description": "Hello world this is my first file stored on IPFS",
      "author": "Gregoire Jeanmart",
      "votes": 10,
      "date_created": 1518700549,
      "tags": ["general"]
  }
}
```

-   **Sample Request:**

```
curl -X POST \
  http://localhost:8040/mahuta/index/file \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F file=@/home/gjeanmart/hello.pdf \
  -F 'request='{"indexName":"articles","indexDocId":"hello_world","contentType":"text/markdown","index_fields":{"title":"Hello world","author":"Gregoire Jeanmart","votes":10,"date_created":1518700549,"tags":["general"]}}'
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
```

---------------------------

#### [Query] Get content

Get content on IPFS by hash

-   **URL** `http://localhost:8040/mahuta/query/fetch/{hash}`
-   **Method:** `GET`
-   **Header:**  `N/A`
-   **URL Params** `N/A`

-   **Sample Request:**

```
$ curl \
    'http://localhost:8040/mahuta/query/fetch/QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o' \
    -o hello_doc.pdf
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:** (file)

---------------------------

#### [Query] Search contents

Search content accross an index using a dedicated query language

-   **URL** `http://localhost:8040/mahuta/query/search`
-   **Method:** `GET` or `POST`
-   **Header:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params**

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| index | String | no |  | Index to search (if null: all indexes) |
| pageNo | Int | no | 0 | Page Number |
| pageSize | Int | no | 20 | Page Size / Limit |
| sort | String | no |  | Sorting attribute |
| dir | ASC/DESC | no | ASC | Sorting direction |
| query | String | no |  | Query |


-   **Data Params**

The `search` operation allows to run a multi-criteria search against an index. The body combines a list of filters :

| Name | Type | Description |
| -------- | -------- | -------- |
| name | String | Index field to perform the search |
| names | String[] | Index fields to perform the search |
| operation | See below | Operation to run against the index field |
| value | Any | Value to compare with |



| Operation | Description |
| -------- | -------- |
| FULL_TEXT | Full text search |
| EQUALS | Equals |
| NOT_EQUALS | Not equals |
| CONTAINS | Contains the word/phrase |
| IN | in the following list |
| GT | Greater than |
| GTE | Greater than or Equals |
| LT | Less than  |
| LTE | Less than or Equals |


```
{
  "query": [
    {
      "name": "title",
      "operation": "CONTAINS",
      "value": "Hello"
    },
    {
      "names": ["description", "title"],
      "operation": "FULL_TEXT",
      "value": "IPFS"
    },
    {
      "name": "votes",
      "operation": "LT",
      "value": "5"
    }
  ]
}
```

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
