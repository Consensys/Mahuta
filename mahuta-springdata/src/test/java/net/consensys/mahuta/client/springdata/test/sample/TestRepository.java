package net.consensys.mahuta.client.springdata.test.sample;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.springdata.impl.MahutaRepositoryImpl;

public class TestRepository extends MahutaRepositoryImpl<Entity, String> {

    public TestRepository(Mahuta mahutas) {
        super(mahutas);
    }

    public Page<Entity> findByAttribute(String key, Object value, Pageable pagination) {

        Query query = Query.newQuery().equals(key, value);

        return this.search(query, pagination);
    }
    
}
