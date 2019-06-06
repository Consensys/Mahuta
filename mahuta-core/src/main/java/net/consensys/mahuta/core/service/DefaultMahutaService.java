package net.consensys.mahuta.core.service;

import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;

/**
 * 
 * 
 * @author gjeanmart<gregoire.jeanmart@gmail.com>
 *
 */
public class DefaultMahutaService extends AbstractMahutaService {

    public DefaultMahutaService(StorageService storageService, IndexingService indexingService) {
        super(storageService, indexingService, false);
    }
}
