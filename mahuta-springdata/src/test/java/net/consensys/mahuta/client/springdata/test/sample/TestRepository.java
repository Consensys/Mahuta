package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Set;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.utils.BytesUtils;
import net.consensys.mahuta.springdata.impl.MahutaRepositoryImpl;

public class TestRepository extends MahutaRepositoryImpl<Entity, String> {

    public TestRepository(Mahuta mahuta, String index, Set<String> indexFields, Class<Entity> entityClazz) {
        super(mahuta, index, indexFields, indexFields, entityClazz, "id", "hash", BytesUtils.readFileInputStream("index_mapping.json"), true);
    }

}
