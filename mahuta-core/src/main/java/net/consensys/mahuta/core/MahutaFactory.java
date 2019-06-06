package net.consensys.mahuta.core;

import net.consensys.mahuta.core.service.AsynchonousPinningMahutaService;
import net.consensys.mahuta.core.service.DefaultMahutaService;
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

    public Mahuta defaultImplementation() {
        return Mahuta.of(new DefaultMahutaService(storageService, indexingService)); 
    }

    public Mahuta asynchronousPinningImplementation(long schedulerPeriod) {
        return Mahuta.of(new AsynchonousPinningMahutaService(storageService, indexingService, schedulerPeriod)); 
    }
    
}
