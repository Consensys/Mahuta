package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Set;

import net.consensys.mahuta.client.java.MahutaClient;
import net.consensys.mahuta.client.springdata.impl.MahutaRepositoryImpl;

public class TestRepository extends MahutaRepositoryImpl<Entity, String> {

    public TestRepository(MahutaClient client, String index, Set<String> indexFields, Class<Entity> entityClazz) {
        super(client, index, indexFields, indexFields, entityClazz);
    }

}
