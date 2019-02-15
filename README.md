Mahuta
======

| Service | Master | Development |
| -------- | -------- | -------- |
| CI Status | ![](https://img.shields.io/circleci/project/github/ConsenSys/IPFS-Store/master.svg) | ![](https://img.shields.io/circleci/project/github/ConsenSys/IPFS-Store/development.svg) |
| Test Coverage | [![Coverage](https://coveralls.io/repos/github/ConsenSys/IPFS-Store/badge.svg?branch=master)](https://coveralls.io/github/ConsenSys/IPFS-Store?branch=master) | [![Coverage](https://coveralls.io/repos/github/ConsenSys/IPFS-Store/badge.svg?branch=development)](https://coveralls.io/github/ConsenSys/IPFS-Store?branch=development) |
| Bintray | [ ![Bintray](https://api.bintray.com/packages/consensys/kauri/ipfs-store/images/download.svg?version=0.2.2) ](https://bintray.com/consensys/kauri/ipfs-store/0.2.2/link) | [ ![Bintray](https://api.bintray.com/packages/consensys/kauri/ipfs-store/images/download.svg) ](https://bintray.com/consensys/kauri/ipfs-store/_latestVersion) |
| Docker | [![](https://img.shields.io/docker/pulls/gjeanmart/mahuta.svg?style=flat)](https://cloud.docker.com/repository/docker/gjeanmart/mahuta)|
| Sonar | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gjeanmart_IPFS-Store&metric=alert_status)](https://sonarcloud.io/dashboard?id=gjeanmart_IPFS-Store) |


**Mahuta** (formerly known as IPFS-Store) is a adaptable search engine opensource tool aiming to collect, store and index data on the IPFS network. This is a convenient solution for any applications storing data on IPFS which require content discovery (complex queries or full text search with fuzziness).

This can be deployed as a simple embedded Java library for your Java application or a simple, scalable and configurable API.

This service also offers extra-features for you IPFS environment such as multi-pinning (replication), smart contract event listening (wip).

[![Mahuta.jpg](https://api.beta.kauri.io:443/ipfs/QmPznCZDvzmEun5qstBQyyLEDfDFqbhuS24Pgsixy1eSnP)](https://postimg.cc/image/mziklo4b1/)


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

TO COMPLETE

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

3. Run the project

```
$ export MAHUTA_IPFS_HOST=localhost
$ export MAHUTA_IPFS_PORT=5001
$ export MAHUTA_ELASTICSEARCH_HOST=localhost
$ export MAHUTA_ELASTICSEARCH_PORT=9300
$ export MAHUTA_ELASTICSEARCH_CLUSTERNAME=cluster_name

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

###### Raw

Enable to store any kind of content. The API uses HTTP multipart to sent the data or file over the request.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| store | Store raw content into IPFS |POST | /mahuta/raw/store |
| index | Indexraw content |POST | /mahuta/raw/index |
| store_index | Store & Index raw content | POST | /mahuta/raw/store_index |

###### JSON

Enable to store JSON document.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| store | Store json content into IPFS |POST | /mahuta/json/store |
| index | Index json content |POST | /mahuta/json/index |
| store_index | Store & Index json content | POST | /mahuta/json/store_index |

##### Query
Represents the read operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| fetch | Get content | GET | /mahuta/query/fetch/{hash} |
| search | Search content | POST | /mahuta/query/search |
| search | Search content | GET | /mahuta/query/search |


#### Configuration

Represents the configuration operations.

| Operation | Description | Method | URI |
| -------- | -------- | -------- | -------- |
| create_index | Create an index in ElasticSearch |POST | /mahuta/config/index/{index} |

[WIKI: API-Documentation](https://github.com/ConsenSys/mahuta/wiki/3.-API-Documentation)


### Details

#### [Persistent / Raw] Store content

Store a content (any type) in IPFS

-   **URL:** `/mahuta/raw/store`    
-   **Method:** `POST`
-   **Header:** `N/A`
-   **URL Params:** `N/A`
-   **Data Params:**
    -   `file: [content]`

-   **Sample Request:**
```
$ curl -X POST \
    'http://localhost:8040/mahuta/raw/store' \
    -F 'file=@/home/gjeanmart/hello.pdf'
```

-   **Success Response:**
    -   **Code:** 200  
        **Content:**
```
{
    "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o"
}
```

---------------------------

#### [Persistent / Raw] Index content

Index IPFS content into the search engine

-   **URL** `/mahuta/raw/index`
-   **Method:** `POST`
-   **Header:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |


-   **URL Params** `N/A`
-   **Data Params**

    - `request:`

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| index | String | yes |  | Index name |
| id | String | no |  | Identifier of the document in the index. id null, autogenerated |
| content_type | String | no |  | Content type (MIMETYPE) |
| hash | String | yes |  | IPFS Hash of the content |
| index_fields | Key/Value[] | no |  | Key/value map presenting IPFS content metadata|


```
{
  "index": "documents",
  "id": "hello_doc",
  "content_type": "application/pdf",
  "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o",
  "index_fields": [
    {
      "name": "title",
      "value": "Hello Doc"
    },
    {
      "name": "author",
      "value": "Gregoire Jeanmart"
    },
    {
      "name": "votes",
      "value": 10
    },
    {
      "name": "date_created",
      "value": 1518700549
    }
  ]
}
```

-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/raw/index' \
    -H 'content-type: application/json' \  
    -d '{"index":"documents","id":"hello_doc","content_type":"application/pdf","hash":"QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o","index_fields":[{"name":"title","value":"Hello Doc"},{"name":"author","value":"Gregoire Jeanmart"},{"name":"votes","value":10},{"name":"date_created","value":1518700549}]}'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**
```
{
    "index": "documents",
    "id": "hello_doc",
    "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o"
}
```

---------------------------

#### [Persistent / Raw] Store & Index content

Store content in IPFS and index it into the search engine

-   **URL** `/mahuta/raw/store_index`
-   **Method:** `POST`
-   **Header:** `N/A`
-   **URL Params** `N/A`
-   **Data Params**


    -   `file: [content]`
    -   `request: `

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| index | String | yes |  | Index name |
| id | String | no |  | Identifier of the document in the index. id null, autogenerated |
| content_type | String | no |  | Content type (MIMETYPE) |
| index_fields | Key/Value[] | no |  | Key/value map presenting IPFS content metadata|


```
{
  "index": "documents",
  "id": "hello_doc",
  "content_type": "application/pdf",
  "index_fields": [
    {
      "name": "title",
      "value": "Hello Doc"
    },
    {
      "name": "author",
      "value": "Gregoire Jeanmart"
    },
    {
      "name": "votes",
      "value": 10
    },
    {
      "name": "date_created",
      "value": 1518700549
    }
  ]
}
```

-   **Sample Request:**

```
curl -X POST \
  http://localhost:8040/mahuta/raw/store_index \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F file=@/home/gjeanmart/hello.pdf \
  -F 'request={"index":"documents","id":"hello_doc","content_type":"application/pdf","index_fields":[{"name":"title","value":"Hello Doc"},{"name":"author","value":"Gregoire Jeanmart"},{"name":"votes","value":10},{"name":"date_created","value":1518700549}]}'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**
```
{
    "index": "documents",
    "id": "hello_doc",
    "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o"
}
```

---------------------------

#### [Persistent / JSON] Store content

Store a JSON document in IPFS

-   **URL:** `/mahuta/json/store`    
-   **Method:** `POST`
-   **Header:**

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params:** `N/A`
-   **Data Params:**     

     -   `payload: {json}`

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| payload | JSON | yes |  | JSON Document to store|

-   **Sample Request:**
```
$ curl -X POST \
    'http://localhost:8040/mahuta/json/store' \
    -H 'Content-Type: application/json' \
    -d '{
      "field1": "val1",
      "field2": 10,
      "field3": {
          "test": true
      }
    }'
```

-   **Success Response:**
    -   **Code:** 200  
        **Content:**
```
{
    "hash": "QmdUNaxwiGT7fzdt6gVpMDFAmjf7dDxMwux16o4s1HyCnD"
}
```

---------------------------

#### [Persistent / JSON] Index content

Index IPFS JSON document into the search engine

-   **URL** `/mahuta/json/index`
-   **Method:** `POST`
-   **Header:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |


-   **URL Params** `N/A`
-   **Data Params**

    - `request:`

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| index | String | yes |  | Index name |
| id | String | no |  | Identifier of the document in the index. id null, autogenerated |
| content_type | String | no |  | Content type (MIMETYPE) |
| hash | String | yes |  | IPFS Hash of the content |
| index_fields | Key/Value[] | no |  | Key/value map presenting IPFS content metadata|


```
{
  "index": "json_documents",
  "id": "json_doc",
  "content_type": "application/json",
  "hash": "QmdUNaxwiGT7fzdt6gVpMDFAmjf7dDxMwux16o4s1HyCnD",
  "index_fields": [
    {
      "name": "field1",
      "value": "val1"
    },
    {
      "name": "external_field",
      "value": 10
    },
    {
      "name": "date_created",
      "value": 1518700549
    }
  ]
}
```

-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/json/index' \
    -H 'content-type: application/json' \  
    -d '{"index":"json_documents","id":"json_doc","content_type":"application/json","hash":"QmdUNaxwiGT7fzdt6gVpMDFAmjf7dDxMwux16o4s1HyCnD","index_fields":[{"name":"field1","value":"val1"},{"name":"external_field","value":10},{"name":"date_created","value":1518700549}]}'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**
```
{
    "index": "json_documents",
    "id": "json_doc",
    "hash": "QmdUNaxwiGT7fzdt6gVpMDFAmjf7dDxMwux16o4s1HyCnD"
}
```

---------------------------

#### [Persistent / JSON] Store & Index content

Store a JSON document in IPFS and index it into the search engine

-   **URL** `/mahuta/json//store_index`
-   **Method:** `POST`
-   **Header:**

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params** `N/A`
-   **Data Params**


    -   `payload: {json}`

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| payload | JSON | yes |  | JSON Document to store|


    -   `request: `


| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| index | String | yes |  | Index name |
| id | String | no |  | Identifier of the document in the index. id null, autogenerated |
| content_type | String | no |  | Content type (MIMETYPE) |
| index_fields | Key/Value[] | no |  | Key/value map presenting IPFS content metadata|


```
{
  "payload": {
    "field1": "val1",
    "field2": 10,
    "field3": {
      "test": true
    }
  },
  "index": "json_documents",
  "id": "doc",
  "content_type": "application/json",
  "index_fields": [
      {
        "name": "type",
        "value": "json"
      },
      {
        "name": "title",
        "value": "json_sample"
      }
  ]
}
```

-   **Sample Request:**

```
curl -X POST \
  http://localhost:8040/mahuta/jsonstore_index \
  -H 'content-type: application/json' \
  -d '{
      "payload": {
        "field1": "val1",
        "field2": 10,
        "field3": {
          "test": true
        }
      },
      "index": "json_documents",
      "id": "doc",
      "content_type": "application/json",
      "index_fields": [
          {
            "name": "type",
            "value": "json"
          },
          {
            "name": "title",
            "value": "json_sample"
          }
      ]
  }'
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**
```
{
    "index": "json_documents",
    "id": "doc",
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
| index | String | no |  | Index to search (if null: all indices) |
| pageNo | Int | no | 0 | Page Number |
| pageSize | Int | no | 20 | Page Size / Limit |
| sort | String | no |  | Sorting attribute |
| dir | ASC/DESC | no | ASC | Sorting direction |
| query | String | no |  | Query URL encoded (for GET call) |


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
| full_text | Full text search |
| equals | Equals |
| not_equals | Not equals |
| contains | Contains the word/phrase |
| in | in the following list |
| gt | Greater than |
| gte | Greater than or Equals |
| lt | Less than  |
| lte | Less than or Equals |


```
{
  "query": [
    {
      "name": "title",
      "operation": "contains",
      "value": "Hello"
    },
    {
      "names": ["author", "title"],
      "operation": "full_text",
      "value": "Gregoire"
    },
    {
      "name": "votes",
      "operation": "lt",
      "value": "5"
    }
  ]
}
```

-   **Sample Request:**

```
curl -X POST \
    'http://localhost:8040/mahuta/query/search?index=documents' \
    -H 'content-type: application/json' \  
    -d '{"query":[{"name":"title","operation":"contains","value":"Hello"},{"name":"author","operation":"equals","value":"Gregoire Jeanmart"},{"name":"votes","operation":"lt","value":"5"}]}'
```


```
curl -X GET \
  'http://localhost:8040/mahuta/query/search?index=documents&page=1&size=2&query=%7B%22query%22%3A%5B%7B%22name%22%3A%22votes%22%2C%22operation%22%3A%22lt%22%2C%22value%22%3A%225%22%7D%5D%7D' \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
```

-   **Success Response:**

    -   **Code:** 200  
        **Content:**

```
{
  "content": [
    {
      "index": "documents",
      "id": "hello_doc",
      "hash": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o",
      "content_type": "application/pdf",
      "index_fields": [
        {
          "name": "__content_type",
          "value": "application/pdf"
        },
        {
          "name": "__hash",
          "value": "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o"
        },
        {
          "name": "title",
          "value": "Hello Doc"
        },
        {
          "name": "author",
          "value": "Gregoire Jeanmart"
        },
        {
          "name": "votes",
          "value": 10
        },
        {
          "name": "date_created",
          "value": 1518700549
        }
      ]
    }
  ]
}
],
"sort": null,
"firstPage": false,
"totalElements": 4,
"lastPage": true,
"totalPages": 1,
"numberOfElements": 4,
"size": 20,
"number": 1
}
```

---------------------------

#### [Configuration] Create an index

Create an index in ElasticSearch


-   **URL:** `/mahuta/config/index/{index}`    
-   **Method:** `POST`
-   **Header:**
-   
| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params:** `N/A`
-   **Data Params:** `N/A`

-   **Sample Request:**
```
$ curl -X POST \
    'http://localhost:8040/mahuta/config/index/MyIndex' \
    -H 'content-type: application/json'
```

-   **Success Response:**
    -   **Code:** 200  
        **Content:** `N/A`



## Advanced Configuration

[WIKI: Configuration](https://github.com/ConsenSys/mahuta/wiki/2.-Configuration)



## Clients

[WIKI: Clients](https://github.com/ConsenSys/mahuta/wiki/5.-Clients)



## Examples

[WIKI: Examples](https://github.com/ConsenSys/mahuta/wiki/6.-Examples)
