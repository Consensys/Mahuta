Mahuta Java API
======

## Configuration

Before starting, the client needs to instantiate a Mahuta object.

### Storage

Storage represents the storage layer (aka IPFS)

```
StorageService storage = IPFSService.connect(String host, Integer port) or .connect(String multiaddress)
    .configureReadTimeout(Integer readTtimeout) // optional, default: 5000ms
    .configureWriteTimeout(Integer writeTimeout) // optional, default: 5000ms
    .configureThreadPool(Integer poolSize) // optional, default: 10
    .configureRetry(Integer maxRetry, Duration delay) // optional, default: 3 times, 0 sec
    .addReplica(PinningService pinningService) // Another IPFSService or an IPFSClusterPinningService
```

### Indexer

Indexer represents the indexing layer (ElasticSearch)

```
IndexerService indexer = ElasticSearchService.connect(String host, Integer port, String clusterName)
    .configureIndexNullValue(boolean indexNullValue) // optiona, default: false, index null values using string "NULL" in order to be able to search
    .withIndex(String indexName) or withIndex(String indexName, InputStream configuration) // Create index on startup (if doesn't exist
```


### Service implementation

Select the service implementation to use:

- DefaultMahutaService: Default implementation (synchronous functions)

```
    .defaultImplementation()
```


- AsynchonousPinningMahutaService: Implementation with asynchronous pinning

```
    .asynchronousPinningImplementation(long schedulerPeriod)
```


### Example

```
Mahuta mahuta = new MahutaFactory()
    .configureStorage(IPFSService.connect("localhost", 5001).configureThreadPool(2))
    .configureIndexer(ElasticSearchService.connect("localhost", 9300, "cluster-name").withIndex("article"))
    .defaultImplementation();
```

## Operations

### Create Index

Create an index

```
CreateIndexResponse response = mahuta.prepareCreateIndex("indexName")
    .configuration() //InputStream: Optional configuration file (on ElasticSearch index mapping JSON)
    .execute();
```

### Get Indexes

Retrieve all indexed from the indexer

```
GetIndexesResponse response = mahuta.prepareGetIndexes()
    .execute();
```

### Index (String)


### Index (CID)


### Index (InputStream)



### Storage 

No indexation



