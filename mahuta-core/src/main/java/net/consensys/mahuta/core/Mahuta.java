package net.consensys.mahuta.core;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.domain.createindex.CreateIndexRequestBuilder;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequestBuilder;
import net.consensys.mahuta.core.domain.get.GetRequestBuilder;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.OnlyStoreIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.search.SearchRequestBuilder;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.service.MahutaService;

/**
 * Mahuta library entry point
 * 
 * @author gjeanmart
 *
 */
public class Mahuta {

    private final MahutaService service;

    private Mahuta(MahutaService service) {
        this.service = service;
    }

    public static Mahuta of(MahutaService service) {
        return new Mahuta(service);
    }

    private <T extends Builder<?, ?>> T prepare(Class<T> clazz) {
        
        try {
            Constructor<T> constructor = clazz.getConstructor(MahutaService.class);
            return constructor.newInstance(service);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new TechnicalException(e);
        }
    }

    public CreateIndexRequestBuilder prepareCreateIndex(String indexName) {
        CreateIndexRequestBuilder builder =  prepare(CreateIndexRequestBuilder.class);
        return builder.name(indexName);
    }

    public GetIndexesRequestBuilder prepareGetIndexes() {
        return prepare(GetIndexesRequestBuilder.class);
    }
    
    public StringIndexingRequestBuilder prepareStringIndexing(String indexName, String content) {
        StringIndexingRequestBuilder builder =  prepare(StringIndexingRequestBuilder.class);
        return builder.indexName(indexName).content(content);
    }
    
    public OnlyStoreIndexingRequestBuilder prepareStorage(InputStream content) {
        OnlyStoreIndexingRequestBuilder builder =  prepare(OnlyStoreIndexingRequestBuilder.class);
        return builder.content(content);
        
    }
    
    public StringIndexingRequestBuilder prepareStringIndexing(StringIndexingRequest request) {
        StringIndexingRequestBuilder builder =  prepare(StringIndexingRequestBuilder.class);
        return builder.request(request);
    }
    
    public CIDIndexingRequestBuilder prepareCIDndexing(String indexName, String cid) {
        CIDIndexingRequestBuilder builder =  prepare(CIDIndexingRequestBuilder.class);
        return builder.indexName(indexName).cid(cid);
    }
    
    public CIDIndexingRequestBuilder prepareCIDndexing(CIDIndexingRequest request) {
        CIDIndexingRequestBuilder builder =  prepare(CIDIndexingRequestBuilder.class);
        return builder.request(request);
    }
    
    public InputStreamIndexingRequestBuilder prepareInputStreamIndexing(String indexName, InputStream content) {
        InputStreamIndexingRequestBuilder builder =  prepare(InputStreamIndexingRequestBuilder.class);
        return builder.indexName(indexName).content(content);
    }
    
    public InputStreamIndexingRequestBuilder prepareInputStreamIndexing(InputStreamIndexingRequest request) {
        InputStreamIndexingRequestBuilder builder =  prepare(InputStreamIndexingRequestBuilder.class);
        return builder.request(request);
    }

    public DeindexingRequestBuilder prepareDeindexing(String indexName, String indexDocId) {
        DeindexingRequestBuilder builder =  prepare(DeindexingRequestBuilder.class);
        return builder.indexName(indexName).indexDocId(indexDocId);
    }

    public GetRequestBuilder prepareGet() {
        return prepare(GetRequestBuilder.class);
    }

    public SearchRequestBuilder prepareSearch() {
        return prepare(SearchRequestBuilder.class);
    }

}
