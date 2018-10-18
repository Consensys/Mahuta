package net.consensys.tools.ipfs.ipfsstore.client.springdata.impl;

import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.java.model.IdAndHash;
import net.consensys.tools.ipfs.ipfsstore.client.java.model.MetadataAndPayload;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.IPFSStoreRepository;

@Slf4j
public class IPFSStoreRepositoryImpl<E, I extends Serializable> extends IPFSStoreCustomRepositoryImpl<E, I> implements IPFSStoreRepository<E, I> {

    @Autowired
    public IPFSStoreRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz) {
        super(client, indexName, indexFields, fullTextFields, entityClazz);
    }

    @Autowired
    public IPFSStoreRepositoryImpl(IPFSStore client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz, String attributeId, String attributeHash) {
        super(client, indexName, indexFields, fullTextFields, entityClazz, attributeId, attributeHash);
    }

    @Override
    public <S extends E> S save(S entity) {
        return this.save(entity, null);
    }

    @Override
    public <S extends E> S save(S entity, Map<String, Object> externalIndexFields) {
        try {
            log.debug("Saving entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields);


            // Identifier
            String id = null;
            try {
                id = this.getId(entity);
                if (id == null) {
                    id = generateID();
                    this.setId(entity, id);
                }
                
            } catch (NoSuchMethodException e) {
                log.warn("No method getId() in the entity");
            }

            // Store and index the entity into IPFS+ElasticSearch through ipfs-store service
            IdAndHash idAndHash = this.client.index(
                    serialize(entity),
                    indexName,
                    id,
                    DEFAULT_CONTENT_TYPE,
                    buildIndexFields(entity, indexFields, externalIndexFields));


            // Add the hash to the entity
            try {
                this.setHash(entity, idAndHash.getHash());
            } catch (NoSuchMethodException e) {
                log.warn("No method setHash(hash) in the entity");
            }

            log.debug("Entity [entity: {}, external_index_fields: {}] saved. hash={}", entity, externalIndexFields, idAndHash.getHash());

            return entity;

        } catch (IPFSStoreException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e) {
            log.error("Error while saving the entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields, e);
            return null;
        }
    }

    @Override
    public E findOne(I id) {
        try {
            log.debug("Retrieve entity [id={}]", id);

            MetadataAndPayload result = this.client.getById(indexName, id.toString());

            if (result == null || result.getPayload() == null || result.getPayload().length == 0) {
                return null;
            }

            E entity = deserialize(result.getPayload(), result.getMetadata().getHash());

            log.debug("Entity [id={}] retrieved. entity={}", id, entity);

            return entity;

        } catch (IPFSStoreException e) {
            log.error("Error while retrieving the entity [id={}]", id, e);
            return null;
        }
    }

    @Override
    public Iterable<E> findAll() {
        PageRequest pageable = new PageRequest(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
        return this.findAll(pageable);
    }

    @Override
    public Iterable<E> findAll(Sort sort) {
        PageRequest pageable = new PageRequest(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, sort);
        return this.findAll(pageable);
    }

    @Override
    public Page<E> findAll(Pageable pageable) {

        return this.search(null, pageable);
    }


    @Override
    public boolean exists(Serializable id) {

        try {
            return (this.client.getMetadataById(indexName, id.toString()) != null);
        } catch (IPFSStoreException e) {
            log.error("Error while retrieving the entity [id={}]", id, e);
            return false;
        }
    }

    /*
     * NOT IMPLEMENTED METHODS
     */

    @Override
    public <S extends E> Iterable<S> save(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(E entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Iterable<? extends E> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<E> findAll(Iterable<I> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Serializable id) {
        throw new UnsupportedOperationException();
    }

}
