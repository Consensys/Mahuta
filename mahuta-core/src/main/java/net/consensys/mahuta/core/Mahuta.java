package net.consensys.mahuta.core;

import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.common.PageRequest.SortDirection;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.searching.Query;
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

    public void createIndex(String indexName) {
        this.service.createIndex(indexName, null);
    }
    
    public void createIndex(String indexName, InputStream configuration) {
        this.service.createIndex(indexName, configuration);
    }

    public Metadata index(InputStream inputSteam, String indexName) {
        return this.index(inputSteam, indexName, null);
    }

    public Metadata index(InputStream inputSteam, String indexName, String id) {
        return this.index(inputSteam, indexName, id, null);
    }

    public Metadata index(InputStream inputSteam, String indexName, String id, String contentType) {
        return this.index(inputSteam, indexName, id, contentType, ImmutableMap.of());

    }

    public Metadata index(InputStream inputSteam, String indexName, String id, String contentType,
            Map<String, Object> indexFields) {
        IndexingRequest request = InputStreamIndexingRequest.build().content(inputSteam).indexName(indexName).indexDocId(id)
                .contentType(contentType).indexFields(indexFields);

        return this.index(request);
    }

    public Metadata index(String cid, String indexName) {
        return this.index(cid, indexName, null);
    }

    public Metadata index(String cid, String indexName, String id) {
        return this.index(cid, indexName, id, null);
    }

    public Metadata index(String cid, String indexName, String id, String contentType) {
        return this.index(cid, indexName, id, contentType, ImmutableMap.of());

    }

    public Metadata index(String cid, String indexName, String id, String contentType, Map<String, Object> indexFields) {
        IndexingRequest request = CIDIndexingRequest.build().content(cid).indexName(indexName).indexDocId(id)
                .contentType(contentType).indexFields(indexFields);

        return this.index(request);
    }

    public Metadata index(IndexingRequest request) {
        return this.service.index(request);
    }

    public void deindex(String indexName, String id) {
        this.deindex(DeindexingRequest.of(indexName, id));
    }

    public void deindex(DeindexingRequest request) {
        this.service.deindex(request);
    }

    public MetadataAndPayload getById(String indexName, String id) {
        return this.service.getByIndexDocId(indexName, id);
    }

    public MetadataAndPayload getByHash(String indexName, String hash) {
        return this.service.getByContentId(indexName, hash);
    }

    public Page<Metadata> search(String indexName) {
        return this.search(indexName, Query.newQuery());
    }

    public Page<Metadata> search(String indexName, Query query) {
        return this.search(indexName, query, PageRequest.of());
    }

    public Page<Metadata> search(String indexName, Query query, int page, int size) {
        return this.search(indexName, query, page, size, null, null);
    }

    public Page<Metadata> search(String indexName, Query query, int page, int size, String sort, SortDirection direction) {
        return this.search(indexName, query, PageRequest.of(page, size, sort, direction));
    }

    public Page<Metadata> search(String indexName, Query query, PageRequest pageRequest) {
        return this.service.search(indexName, query, pageRequest);
    }

    public Page<MetadataAndPayload> searchAndFetch(String indexName) {
        return this.searchAndFetch(indexName, Query.newQuery());
    }

    public Page<MetadataAndPayload> searchAndFetch(String indexName, Query query) {
        return this.searchAndFetch(indexName, query, PageRequest.of());
    }

    public Page<MetadataAndPayload> searchAndFetch(String indexName, Query query, int page, int size) {
        return this.searchAndFetch(indexName, query, page, size, null, null);
    }

    public Page<MetadataAndPayload> searchAndFetch(String indexName, Query query, int page, int size, String sort,
            SortDirection direction) {
        return this.searchAndFetch(indexName, query, PageRequest.of(page, size, sort, direction));
    }

    public Page<MetadataAndPayload> searchAndFetch(String indexName, Query query, PageRequest pageRequest) {
        return this.service.searchAndFetch(indexName, query, pageRequest);
    }

}
