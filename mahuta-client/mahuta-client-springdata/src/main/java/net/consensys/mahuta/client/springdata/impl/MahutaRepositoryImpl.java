package net.consensys.mahuta.client.springdata.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.client.java.MahutaClient;
import net.consensys.mahuta.client.java.model.IdAndHash;
import net.consensys.mahuta.client.java.model.MetadataAndPayload;
import net.consensys.mahuta.client.springdata.MahutaRepository;
import net.consensys.mahuta.exception.MahutaException;
import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.exception.TechnicalException;

@Slf4j
public class MahutaRepositoryImpl<E, I extends Serializable> extends MahutaCustomRepositoryImpl<E, I> implements MahutaRepository<E, I> {

    @Autowired
    public MahutaRepositoryImpl(MahutaClient client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz) {
        super(client, indexName, indexFields, fullTextFields, entityClazz);
    }

    @Autowired
    public MahutaRepositoryImpl(MahutaClient client, String indexName, Set<String> indexFields, Set<String> fullTextFields, Class<E> entityClazz, String attributeId, String attributeHash) {
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

        } catch (MahutaException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e) {
            log.error("Error while saving the entity [entity: {}, external_index_fields: {}]", entity, externalIndexFields, e);
            return null;
        }
    }

    @Override
    public Optional<E> findById(I id) {
        try {
            log.debug("Retrieve entity [id={}]", id);

            MetadataAndPayload result = this.client.getById(indexName, id.toString());

            if (result == null || result.getPayload() == null || result.getPayload().length == 0) {
                return Optional.empty();
            }

            E entity = deserialize(result.getPayload(), result.getMetadata().getHash());

            log.debug("Entity [id={}] retrieved. entity={}", id, entity);

            return Optional.ofNullable(entity);

        } catch (NotFoundException e) {
            log.error("Entity not found [id={}]", id);
            return Optional.empty();
            
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    @Override
    public Iterable<E> findAll() {
        PageRequest pageable = PageRequest.of(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
        return this.findAll(pageable);
    }

    @Override
    public Iterable<E> findAll(Sort sort) {
        PageRequest pageable = PageRequest.of(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, sort);
        return this.findAll(pageable);
    }

    @Override
    public Page<E> findAll(Pageable pageable) {

        return this.search(null, pageable);
    }


    @Override
    public boolean existsById(Serializable id) {

        try {
            return (this.client.getMetadataById(indexName, id.toString()) != null);
        } catch (MahutaException e) {
            log.error("Error while retrieving the entity [id={}]", id, e);
            return false;
        }
    }

    /*
     * NOT IMPLEMENTED METHODS
     */

    @Override
    public <S extends E> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(E entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends E> entities) {
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
    public Iterable<E> findAllById(Iterable<I> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(Serializable id) {
        throw new UnsupportedOperationException();
    }

}
