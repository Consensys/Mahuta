package net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample;

import java.util.Set;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.impl.IPFSStoreRepositoryImpl;

public class TestRepository extends IPFSStoreRepositoryImpl<Entity, String> {

    public TestRepository(IPFSStore client, String index, Set<String> indexFields, Class<Entity> entityClazz) {
        super(client, index, indexFields, null, entityClazz);
    }

}
