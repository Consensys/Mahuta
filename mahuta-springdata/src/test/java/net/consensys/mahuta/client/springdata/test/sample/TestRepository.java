package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.utils.BytesUtils;
import net.consensys.mahuta.springdata.impl.MahutaRepositoryImpl;

public class TestRepository extends MahutaRepositoryImpl<Entity, String> {

    public TestRepository(Mahuta mahuta, String index, Set<String> indexFields, Class<Entity> entityClazz) {
        super(mahuta, index, indexFields, indexFields, entityClazz, "id", "hash", BytesUtils.readFileInputStream("index_mapping.json"), true);
    }

    public Page<Entity> findByAttribute(String key, Object value, Pageable pagination) {

        Query query = Query.newQuery().equals(key, value);

        return this.search(query, pagination);
    }
    
}
