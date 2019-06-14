Mahuta Spring Data
======

Mahuta Spring data allows to use Spring-data abstraction repository against Mahuta to easily store and index content on IPFS.

The content is serialised in JSON using Jackson.


## Entity

Define your domain entity :

```
@IPFSDocument(index = "entity", indexConfiguration = "index_mapping.json", indexContent = true)
public class Entity {
    
    @Id
    private String id;

    @Hash
    private String hash;

    @Indexfield @Fulltext
    private String name;

    @Indexfield
    private int age;

    @Indexfield
    private Set<String> tags;
}

```

`@IPFSDocument`: optional Class annotation to configure the index with

- indexName (optional): Specify the name of the index (default is entity class name)
- indexConfiguration (optional): Specify a configuration file (classpath) for the index
- indexContent (optional, default: false): Cache the content in the index

 `@Id`: optional Field annotation to specify the field used to read and write the ID identifying the file
 
 `@Hash`: optional Field annotation to specify a container to get the contentId after saving
 
 `@Indexfield`: Optional Field annotation to indicate that the field must be indexed
 
 `@Fulltext`: Optional Field annotation to indicate that the field must be indexed and used for fulltext search


## Repository


Setup the repository:


| Operation | 
| -------- |
| <E> S save(S entity) |
| <E> S save(S entity, Map<String, Object> externalIndexFields) |
| Optional<E> findById(ID id) |
| Iterable<E> findAll() |
| Iterable<E> findAll(Sort sort) |
| Page<E> findAll(Pageable pageable) |
| boolean existsById(ID id) |
| void deleteById(ID id) |
| void updateIndexField(ID id, String field, Object value) |



```
public class EntityRepository extends MahutaRepositoryImpl<Entity, String> {

    public EntityRepository(Mahuta mahuta) {
        super(mahuta); // <<< Mahuta instance
    }
}
```