package net.consensys.mahuta.springdata.impl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.springdata.MahutaRepository;
import net.consensys.mahuta.springdata.exception.MahutaSpringDataRuntimeException;

@Slf4j
public class MahutaRepositoryImpl<E, I extends Serializable> extends MahutaCustomRepositoryImpl<E>
        implements MahutaRepository<E, I> {

    @Autowired
    public MahutaRepositoryImpl(Mahuta mahuta) {
        super(mahuta);
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
            if(attributeId.isPresent()) {
                id = (String) attributeId.get().invokeGetter(entity, ID_CLASS);
                if(id == null) {
                    id = generateID();
                    attributeId.get().invokeSetter(entity, id);
                }
            }
            
            IndexingResponse response = mahuta.prepareInputStreamIndexing(indexName, serialize(entity))
                    .contentType(DEFAULT_CONTENT_TYPE)
                    .indexDocId(id)
                    .indexFields(buildIndexFields(entity, indexFields, externalIndexFields))
                    .indexContent(indexContent)
                    .execute();

            // Add the hash to the entity
            if(attributeHash.isPresent()) {
                attributeHash.get().invokeSetter(entity, response.getContentId());
            }

            log.debug("Entity [entity: {}, external_index_fields: {}] saved. hash={}", entity, externalIndexFields,
                    response.getContentId());

            return entity;

        } catch (JsonProcessingException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("Error while saving the entity [entity: {}, external_index_fields: {}]", entity,
                    externalIndexFields, e);
            return null;
        }
    }

    @Override
    public Optional<E> findById(I id) {
        try {
            log.debug("Retrieve entity [id={}]", id);

            GetResponse response = mahuta.prepareGet()
                    .indexName(indexName)
                    .indexDocId(id.toString())
                    .loadFile(true)
                    .execute();

            E entity = deserialize(response.getPayload(), response.getMetadata().getContentId());

            log.debug("Entity [id={}] retrieved. entity={}", id, entity);

            return Optional.ofNullable(entity);

        } catch (NotFoundException e) {
            log.error("Entity not found [id={}]", id);
            return Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException | IOException e) {
            log.error("Error while deserialising object", e);
            throw new MahutaSpringDataRuntimeException("Error while deserialising object", e);
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
            mahuta.prepareGet()
                .indexName(indexName)
                .indexDocId(id.toString())
                .execute();
            
            return true;

        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void deleteById(Serializable id) {
        mahuta.prepareDeindexing(indexName, id.toString())
            .execute();
    }

    @Override
    public void updateIndexField(Serializable id, String field, Object value) {

        log.debug("updateIndexField [id={}, field: {}, value: {}]", id, field, value);

        mahuta.prepareUpdateField(indexName, id.toString(), field, value)
                .execute();
        
        log.debug("Field [id={}, field: {}, value: {}] updated", id, field, value);
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

}
