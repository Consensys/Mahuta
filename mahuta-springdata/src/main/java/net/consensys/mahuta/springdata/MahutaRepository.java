package net.consensys.mahuta.springdata;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * MahutaRepository represents the union of a PagingAndSortingRepository and a MahutaCustomRepository
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public interface MahutaRepository<E, I extends Serializable> extends PagingAndSortingRepository<E, I>, MahutaCustomRepository<E> {


    /**
     * Save a document with external indexes
     *
     * @param entity Entity to save
     * @param externalIndexFields Index fields to indexed which aren't part of the entity
     * @return Entity saved
     */
    <S extends E> S save(S entity, Map<String, Object> externalIndexFields);
    
    /**
     * Update a field in the index
     * @param id Entity ID
     * @param field Field key
     * @param value Field value
     */
    void updateIndexField(I id, String field, Object value);
}

