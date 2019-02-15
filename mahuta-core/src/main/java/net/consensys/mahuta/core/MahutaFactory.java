package net.consensys.mahuta.core;

import net.consensys.mahuta.core.service.MahutaServiceImpl;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;

/**
 * Mahuta factory 
 * Can be used to configure and build a Mahuta object.
 * 
 * @author gjeanmart
 *
 */
public class MahutaFactory {

    private StorageService storageService;
    private IndexingService indexingService;
    
    public MahutaFactory configureStorage(StorageService storageService) {
        this.storageService = storageService;
        return this;
    }

    public MahutaFactory configureIndexer(IndexingService indexingService) {
        this.indexingService = indexingService;
        return this;
    }

    public Mahuta build() {
        return Mahuta.of(new MahutaServiceImpl(storageService, indexingService)); 
    }
    
}
