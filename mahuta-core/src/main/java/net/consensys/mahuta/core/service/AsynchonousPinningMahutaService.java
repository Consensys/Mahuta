package net.consensys.mahuta.core.service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;

/**
 * 
 * 
 * @author gjeanmart<gregoire.jeanmart@gmail.com>
 *
 */
@Slf4j
public class AsynchonousPinningMahutaService extends AbstractMahutaService {
    private static final Integer SCHEDULER_THREAD_POOL = 1;
    private static final Integer SCHEDULER_INITIAL_DELAY = 0;
    private static final Integer PAGE_SIZE = 50;
    
    private final ScheduledExecutorService scheduler;

    public AsynchonousPinningMahutaService(StorageService storageService, IndexingService indexingService, long schedulerPeriod) {
        super(storageService, indexingService, true);

        log.info("Start scheduled pinning process [thread-pool: {}, delay: {}, period: {}]", 
                SCHEDULER_THREAD_POOL, SCHEDULER_INITIAL_DELAY, schedulerPeriod);
        this.scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL);
        this.scheduler.scheduleAtFixedRate(this::run, SCHEDULER_INITIAL_DELAY, schedulerPeriod, TimeUnit.MILLISECONDS);
        
    }

    /**
     * run
     * Pin all files with the flag __pinned=false
     */
    public void run() {
        log.debug("Run asynchromous pinning process");
        
        final Query query = Query.newQuery().equals(IndexingService.PINNED_KEY, false);
        
        indexingService.getIndexes().forEach(indexName -> {
            log.trace("indexName: {}", indexName);
            Page<Metadata> page = null;
            do {
                PageRequest pageReq = Optional.ofNullable(page)
                    .map(Page::nextPageRequest)
                    .orElse(PageRequest.of(0, PAGE_SIZE));
                
                page = indexingService.searchDocuments(indexName, query, pageReq);
                
                page.getElements().forEach(m -> 
                    storageService.getReplicaSet().forEach(pinningService -> {
                        // Pin
                        pinningService.pin(m.getContentId());
                        
                        // Set the flag __pinned to true
                        indexingService.updateField(indexName, m.getIndexDocId(), IndexingService.PINNED_KEY, true);
                    })
                );
            } while(!page.isLast());
        });
    }
}
