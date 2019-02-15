package net.consensys.mahuta.core.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.consensys.mahuta.core.domain.common.Content;
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.createindex.CreateIndexRequest;
import net.consensys.mahuta.core.domain.createindex.CreateIndexResponse;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequest;
import net.consensys.mahuta.core.domain.deindexing.DeindexingResponse;
import net.consensys.mahuta.core.domain.get.GetRequest;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesRequest;
import net.consensys.mahuta.core.domain.getindexes.GetIndexesResponse;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.OnylStoreIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.domain.search.SearchRequest;
import net.consensys.mahuta.core.domain.search.SearchResponse;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.exception.ValidationException;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.utils.ValidatorUtils;
import net.consensys.mahuta.core.utils.lamba.Throwing;

/**
 * 
 * 
 * @author gjeanmart<gregoire.jeanmart@gmail.com>
 *
 */
public class MahutaServiceImpl implements MahutaService {
    private static final String REQUEST = "request";
    
    private StorageService storageService;
    private IndexingService indexingService;

    public MahutaServiceImpl(StorageService storageService, IndexingService indexingService) {
        ValidatorUtils.rejectIfNull("storageService", storageService, "Configure the storage service");
        ValidatorUtils.rejectIfNull("indexingService", indexingService, "Configure the indexer service");

        this.storageService = storageService;
        this.indexingService = indexingService;
    }

    @Override
    public CreateIndexResponse createIndex(CreateIndexRequest request) {
        ValidatorUtils.rejectIfNull(REQUEST, request);

        indexingService.createIndex(request.getName(), request.getConfiguration());

        return CreateIndexResponse.of();
    }

    @Override
    public GetIndexesResponse getIndexes(GetIndexesRequest request) {
        List<String> indexes = indexingService.getIndexes();

        return GetIndexesResponse.of().indexes(indexes);
    }

    public IndexingResponse index(IndexingRequest request) {

        ValidatorUtils.rejectIfNull(REQUEST, request);

        // Write content
        String contentId = null;
        String contentType = request.getContentType();

        if (request instanceof InputStreamIndexingRequest) {
            InputStream content = ((InputStreamIndexingRequest) request).getContent();
            contentId = storageService.write(content);
            contentType = Optional.ofNullable(contentType)
                    .orElseGet(Throwing.rethrowSupplier(() -> URLConnection.guessContentTypeFromStream(content)));

        } else if (request instanceof CIDIndexingRequest) {
            String cid = ((CIDIndexingRequest) request).getCid();
            contentId = cid;

        } else if (request instanceof StringIndexingRequest) {
            byte[] content = ((StringIndexingRequest) request).getContent().getBytes();
            contentId = storageService.write(content);

        } else if (request instanceof OnylStoreIndexingRequest) {
            InputStream content = ((OnylStoreIndexingRequest) request).getContent();
            contentId = storageService.write(content);
            
            return IndexingResponse.of(contentId);

        } else {
            throw new UnsupportedOperationException(request.getClass().getName() + " isn't supported yet");
        }

        // Index content
        String indexDocId = indexingService.index(request.getIndexName(), request.getIndexDocId(), contentId,
                contentType, request.getIndexFields());

        // Pin content
        Content content = Content.of(contentId);
        storageService.getReplicaSet().forEach(pinningService ->
            CompletableFuture.supplyAsync(() -> {
                pinningService.pin(content.getContentId());
                return true;
            })
        );
        
        // Result 
        return IndexingResponse.of(request.getIndexName(), indexDocId, contentId, contentType,
                request.getIndexFields());
    }

    @Override
    public DeindexingResponse deindex(DeindexingRequest request) {

        ValidatorUtils.rejectIfNull(REQUEST, request);

        Metadata metadata = indexingService.getDocument(request.getIndexName(), request.getIndexDocId());

        indexingService.deindex(request.getIndexName(), request.getIndexDocId());

        storageService.getReplicaSet()
            .forEach(pinningService -> pinningService.unpin(metadata.getContentId()));

        return DeindexingResponse.of();
    }

    @Override
    public GetResponse get(GetRequest request) {

        ValidatorUtils.rejectIfNull(REQUEST, request);

        // Metadata
        Metadata metadata = null;
        if (!ValidatorUtils.isEmpty(request.getIndexDocId())) {
            metadata = indexingService.getDocument(request.getIndexName(), request.getIndexDocId());

        } else if (!ValidatorUtils.isEmpty(request.getContentId())) {

            Query query = Query.newQuery().equals(IndexingService.HASH_INDEX_KEY, request.getContentId());
            Page<Metadata> result = indexingService.searchDocuments(request.getIndexName(), query,
                    PageRequest.singleElementPage());

            if (result.isEmpty()) {
                throw new NotFoundException("No Document found for contentId=" + request.getIndexDocId()
                        + " in the index " + request.getIndexDocId());
            }

            metadata = result.getElements().get(0);

        } else {
            throw new ValidationException("request must contain 'indexDocId' or 'contentId'");
        }

        // Payload
        OutputStream payload = null;
        if (request.isLoadFile()) {
            payload = storageService.read(metadata.getContentId());
        }

        return GetResponse.of().metadata(metadata).payload(payload);
    }

    @Override
    public SearchResponse search(SearchRequest request) {

        ValidatorUtils.rejectIfNull(REQUEST, request);

        Page<Metadata> metadatas = indexingService.searchDocuments(request.getIndexName(), request.getQuery(),
                request.getPageRequest());

        List<MetadataAndPayload> elements = metadatas.getElements().stream().map(m -> {
            MetadataAndPayload mp = new MetadataAndPayload();
            mp.setMetadata(m);
            if (request.isLoadFile()) {
                mp.setPayload(storageService.read(m.getContentId()));
            }
            return mp;
        }).collect(Collectors.toList());

        return SearchResponse.of().result(Page.of(request.getPageRequest(), elements, metadatas.getTotalElements()));
    }
}
