package net.consensys.tools.ipfs.ipfsstore.client.springdata.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.IPFSStoreRepository;

public class IPFSStoreRepositoryImpl<E, ID extends Serializable> extends IPFSStoreCustomRepositoryImpl<E, ID> implements IPFSStoreRepository<E, ID> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreRepositoryImpl.class);

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
            LOGGER.debug("Saving entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields);


            // Identifier
            String id = this.getId(entity);
            if (id == null) {
                id = generateID();
                this.setId(entity, id);
            }

            // Store and index the entity into IPFS+ElasticSearch through ipfs-store service
            String hash = this.client.index(
                    serialize(entity),
                    indexName,
                    id,
                    DEFAULT_CONTENT_TYPE,
                    buildIndexFields(entity, indexFields, externalIndexFields));


            // Add the hash to the entity
            this.setHash(entity, hash);

            LOGGER.debug("Entity [entity: {}, external_index_fields: {}] saved. hash={}", entity, externalIndexFields, hash);

            return entity;

        } catch (IPFSStoreException |
                NoSuchMethodException |
                SecurityException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e) {
            LOGGER.error("Error while saving the entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields, e);
            return null;
        }
    }

    @Override
    public E findOne(ID id) {
        try {
            LOGGER.debug("Retrieve entity [id={}]", id);

            byte[] content = this.client.getById(indexName, id.toString());

            if (content == null || content.length == 0) {
                return null;
            }

            E entity = deserialize(content);

            LOGGER.debug("Entity [id={}] retrieved. entity={}", id, entity);

            return entity;

        } catch (IPFSStoreException e) {
            LOGGER.error("Error while retrieving the entity [id={}]", id, e);
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
            LOGGER.error("Error while retrieving the entity [id={}]", id, e);
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
    public Iterable<E> findAll(Iterable<ID> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Serializable id) {
        throw new UnsupportedOperationException();
    }

}
