IPFS-Store
======

**IPFS-Store** is a search engine opensource tool aiming to collect, store and index data on the IPFS network. This is a convenient solution for any applications requiring content discovery (conditional queries or full text search with fuzziness) in a set of IPFS files. This service also offers extra-features for you IPFS environment such as multi-pinning (replication), smart contract event listening (wip).

IPFS-Store can be deployed as a simple, scalable and configurable API and comes with client libraries (Java, JavaScript) to easily integrate it in an application.

A request requires the following the information:

- a payload (JSON or multipart file) - stored on IPFS
- some metadata (index fields) - indexed on a search engine alongside the payload IPFS hash


[![IPFS-_Store_-_architecture.jpg](https://imgur.com/a/YzCDQo8)](https://imgur.com/a/YzCDQo8)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 


### Docker

#### Prerequisites

IPFS-Store depends of two components: 
- an IPFS node ([go](https://github.com/ipfs/go-ipfs) or [js](https://github.com/ipfs/js-ipfs) implementation)
- a search engine (currently only ElasticSearch is supported)

##### Create a virtual network

Let's create a private Docker network to make our containers able to communicate together.

```
$ docker network create ipfs-store-network
```

##### Start IPFS daemon

Start an IPFS daemon to join the IPFS network and expore the port 4001 (peer2p networking) and 5001(API)

```
$ docker run -d --name ipfs -v /path/to/ipfs/export:/export -v /path/to/ipfs/data:/data/ipfs -p :4001:4001 -p :5001:5001 ipfs/go-ipfs:latest
```

##### Start ElasticSearch

Start ElasticSearch database used as a content indexer.

```
$ docker run -d --name elasticsearch -v /path/to/elasticsearch:/data/elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --net ipfs-store-network docker.elastic.co/elasticsearch/elasticsearch:6.5.0
```

#### Start IPFS-Store

Finally we can run IPFS-Store using the port 8040.

```
$ docker run --name ipfs_store -p 8040:8040 -e IPFS_HOST=ipfs -e ELASTIC_HOST=elasticsearch --net ipfs-store-network  gjeanmart/ipfs-store
```

A [docker-compose file](https://github.com/ConsenSys/IPFS-Store/blob/master/ipfs-store-service/docker-compose.yml) can also be used to start IPFS-Store and its dependencies.

To stop the containers, run  `$ docker stop ipfs elasticsearch ipfs_store`.

### From the source

#### Prerequisites

- Java 8
- Maven
- For elasticsearch, set [vm.max_map_count](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html#docker-cli-run-prod-mode) to at least 262144 on the host

#### Build

1. After checking out the code, navigate to the root directory

```
$ cd /path/to/ipfs-store/ipfs-store-service/
```

2. Compile, test and package the project

```
$ mvn clean package
```

3. Run the project

```
$ export IPFS_HOST=localhost
$ export IPFS_PORT=5001
$ export ELASTIC_HOST=localhost
$ export ELASTIC_PORT=9300
$ export ELASTIC_CLUSTERNAME=elasticsearch

$ java -jar target/ipfs-store-exec.jar
```

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


### Details

#### [Persistent / Raw] Store content

Store a content (any type) in IPFS 

-   **URL:** `/ipfs-store/raw/store`    
-   **Method:** `POST`
-   **Header:** `N/A`
-   **URL Params:** `N/A`
-   **Data Params:** 
    -   `file: [content]`

-   **Sample Request:**
```
$ curl -X POST \
    'http://localhost:8040/ipfs-store/raw/store' \
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

-   **URL** `/ipfs-store/raw/index`
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
    'http://localhost:8040/ipfs-store/raw/index' \
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

-   **URL** `/ipfs-store/raw/store_index`
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
  http://localhost:8040/ipfs-store/raw/store_index \
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

-   **URL:** `/ipfs-store/json/store`    
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
    'http://localhost:8040/ipfs-store/json/store' \
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

-   **URL** `/ipfs-store/json/index`
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
    'http://localhost:8040/ipfs-store/json/index' \
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

-   **URL** `/ipfs-store/json//store_index`
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
  http://localhost:8040/ipfs-store/jsonstore_index \
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

-   **URL** `http://localhost:8040/ipfs-store/query/fetch/{hash}`
-   **Method:** `GET`
-   **Header:**  `N/A`
-   **URL Params** `N/A`
    
-   **Sample Request:**
    
```
$ curl \
    'http://localhost:8040/ipfs-store/query/fetch/QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o' \
    -o hello_doc.pdf 
``` 
    
-   **Success Response:**
    
    -   **Code:** 200  
        **Content:** (file)

---------------------------

#### [Query] Search contents

Search content accross an index using a dedicated query language

-   **URL** `http://localhost:8040/ipfs-store/query/search`
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
    'http://localhost:8040/ipfs-store/query/search?index=documents' \
    -H 'content-type: application/json' \  
    -d '{"query":[{"name":"title","operation":"contains","value":"Hello"},{"name":"author","operation":"equals","value":"Gregoire Jeanmart"},{"name":"votes","operation":"lt","value":"5"}]}'
``` 
   
    
```
curl -X GET \
  'http://localhost:8040/ipfs-store/query/search?index=documents&page=1&size=2&query=%7B%22query%22%3A%5B%7B%22name%22%3A%22votes%22%2C%22operation%22%3A%22lt%22%2C%22value%22%3A%225%22%7D%5D%7D' \
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


-   **URL:** `/ipfs-store/config/index/{index}`    
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
    'http://localhost:8040/ipfs-store/config/index/MyIndex' \
    -H 'content-type: application/json' 
```

-   **Success Response:**
    -   **Code:** 200  
        **Content:** `N/A`



## Advanced Configuration

[WIKI: Configuration](https://github.com/ConsenSys/IPFS-Store/wiki/2.-Configuration)



## Clients

[WIKI: Clients](https://github.com/ConsenSys/IPFS-Store/wiki/5.-Clients)



## Examples

[WIKI: Examples](https://github.com/ConsenSys/IPFS-Store/wiki/6.-Examples)
