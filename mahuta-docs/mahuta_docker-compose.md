Mahuta Docker
======

This page explains how to use Mahuta HTTP-API Docker image using docker-compose to build a fully working local environment along side with IPFS and ElaticSearch. 

Mahuta HTTP-API configuration looks by default like this:


```yaml
mahuta:
  ipfs:
    host: localhost
    port: 5001
#    multiaddress: 
    timeout:
       read: 5000
       write: 2000
    threadPool: 10
    replicaIPFS:
    - host: 
      port: 
    replicaClusterIPFS:
    - host: 
      port: 
    
  elasticsearch:
    host: localhost
    port: 9300
    clusterName: docker-cluster
    indexNullValue: "true"
    indexConfigs:
    - name: 
    
  security:
    cors:
      origins: "*"
      methods: "GET,POST"
      headers: "Origin,Content-Type,Accept"
      credentials: "false"
```

There are two ways to configure Mahuta when using Docker:

- With Environment variables 
- With an external YAML configuration file



# Environment variables

It is possible to override a variable with an environment variable using the following rule prop1.property-2.prop3 = PROP1\_PROPERTY-2\_PROP3

| YAML | Environment Variable |
| -------- | -------- |
| mahuta.ipfs.host | MAHUTA\_IPFS\_HOST |
| mahuta.ipfs.replicaIPFS[0].host | MAHUTA\_IPFS\_REPLICA_IPFS_0_HOST |

*docker-compose.yml*

```yaml
version: '2'

services:

  mahuta:
    image: gjeanmart/mahuta:latest
    ports:
    - "8040:8040"
    depends_on:
      - ipfs
      - elasticsearch
    environment:
      WAIT_HOSTS: elasticsearch:9300, ipfs:5001
      LOG_LEVEL: INFO
      MAHUTA_ELASTICSEARCH_HOST: elasticsearch
      MAHUTA_ELASTICSEARCH_PORT: 9300
      MAHUTA_IPFS_HOST: ipfs
      MAHUTA_IPFS_REPLICAIPFS_0_MULTIADDRESS: /dnsaddr/ipfs.infura.io/tcp/5001/https
    networks:
      - default

  elasticsearch:
    image:  docker.elastic.co/elasticsearch/elasticsearch-oss:6.5.0 
    ports:
          - "9200:9200"
          - "9300:9300"
    environment:
      - cluster.name=docker-cluster
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    networks:
      - default
          
  ipfs:
    image: ipfs/go-ipfs
    ports:
          - "4001:4001"
          - "5001:5001"
          - "8081:8080"
```


```
$ docker-compose up
Ctrl+C to stop
```

# External YAML configuration file

Is is also possible to pass a YAML configuration file to the container and load the properties from there.

*external-conf.yml*

```yaml
mahuta:
  ipfs:
    host: ipfs
    port: 5001
    replicaIPFS:
    - multiaddress: /dnsaddr/ipfs.infura.io/tcp/5001/https
      port: 5001
    
  elasticsearch:
    host: elasticsearch
    port: 9300
    clusterName: docker-cluster
    indexNullValue: "true"
    indexConfigs:
    - name: indexToCreate
```

```yaml
version: '2'

services:

  mahuta:
    build: ./
    image: gjeanmart/mahuta:latest
    ports:
    - "8040:8040"
    volumes:
      - ./external-conf.yml:/data/conf.yml
    depends_on:
      - ipfs
      - elasticsearch
    environment:
      WAIT_HOSTS: elasticsearch:9300, ipfs:5001
      CONF: file:///data/conf.yml
      LOG_LEVEL: TRACE
    networks:
      - default

  elasticsearch:
    image:  docker.elastic.co/elasticsearch/elasticsearch-oss:6.5.0 
    ports:
          - "9200:9200"
          - "9300:9300"
    environment:
      - cluster.name=docker-cluster
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    networks:
      - default
          
  ipfs:
    image: ipfs/go-ipfs
    ports:
          - "4001:4001"
          - "5001:5001"
          - "8081:8080"
```


```
$ docker-compose up
Ctrl+C to stop
```

