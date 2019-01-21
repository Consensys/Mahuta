package net.consensys.mahuta.core.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.MetadataAndPayload;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.deindexing.DeindexingRequest;
import net.consensys.mahuta.core.domain.indexing.ByteArrayIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.exception.NotFoundException;
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

    private StorageService storageService;
    private IndexingService indexingService;

    public MahutaServiceImpl(StorageService storageService, IndexingService indexingService) {
        this.storageService = storageService;
        this.indexingService = indexingService;
    }
    
    @Override
    public void createIndex(String indexName, InputStream configuration) {
        ValidatorUtils.rejectIfNull("indexingService", indexingService, "Configure the indexer");
        ValidatorUtils.rejectIfNull("indexName", indexName);

        indexingService.createIndex(indexName, configuration);
    }

    public Metadata index(IndexingRequest request) {

        ValidatorUtils.rejectIfNull("request", request);
        ValidatorUtils.rejectIfNull("storageService", storageService, "Configure the storage");
        ValidatorUtils.rejectIfNull("indexingService", indexingService, "Configure the indexer");

        // Write content
        String contentId = null;
        String contentType = request.getContentType();

        if (request instanceof InputStreamIndexingRequest) {
            InputStream content = ((InputStreamIndexingRequest) request).getContent();
            contentId = storageService.write(content);
            contentType = Optional.ofNullable(contentType)
                    .orElseGet(Throwing.rethrowSupplier(() -> URLConnection.guessContentTypeFromStream(content)));

        } else if (request instanceof ByteArrayIndexingRequest) {
            byte[] content = ((ByteArrayIndexingRequest) request).getContent();
            contentId = storageService.write(content);

        } else if (request instanceof CIDIndexingRequest) {
            String cid = ((CIDIndexingRequest) request).getCid();
            storageService.pin(cid);
            contentId = cid;

        } else if (request instanceof StringIndexingRequest) {
            byte[] content = ((StringIndexingRequest) request).getContent().getBytes();
            contentId = storageService.write(content);

        } else {
            throw new UnsupportedOperationException(request.getClass().getName() + " isn't supported yet");
        }

        // Index content
        String indexDocId = indexingService.index(request.getIndexName(), request.getIndexDocId(), contentId,
                contentType, request.getIndexFields());

        // Result
        return Metadata.of(request.getIndexName(), indexDocId, contentId, contentType, request.getIndexFields());
    }

    @Override
    public void deindex(DeindexingRequest request) {

        MetadataAndPayload metadata = this.getByIndexDocId(request.getIndexName(), request.getId());

        indexingService.deindex(request.getIndexName(), request.getId());

        storageService.unpin(metadata.getMetadata().getContentId());
    }

    @Override
    public MetadataAndPayload getByIndexDocId(String indexName, String indexDocId) {

        ValidatorUtils.rejectIfEmpty("indexName", indexName);
        ValidatorUtils.rejectIfEmpty("indexDocId", indexDocId);

        // Find file in index
        Metadata metadata = indexingService.getDocument(indexName, indexDocId);
        ValidatorUtils.rejectIfNull("metadata", metadata);

        // Get file
        OutputStream payload = storageService.read(metadata.getContentId(), new ByteArrayOutputStream());

        return MetadataAndPayload.of(metadata, payload);
    }

    @Override
    public MetadataAndPayload getByContentId(String indexName, String contentId) {

        Query query = Query.newQuery().equals(IndexingService.HASH_INDEX_KEY, contentId);
        PageRequest pageRequest = PageRequest.singleElementPage();

        Page<MetadataAndPayload> result = this.searchAndFetch(indexName, query, pageRequest);

        if (result.isEmpty()) {
            throw new NotFoundException("No Document found for contentId=" + contentId + " in the index " + indexName);
        }

        return result.getContent().get(0);
    }

    @Override
    public Page<Metadata> search(String indexName, Query query, PageRequest pageRequest) {

        return indexingService.searchDocuments(indexName, query, pageRequest);
    }

    @Override
    public Page<MetadataAndPayload> searchAndFetch(String indexName, Query query, PageRequest pageRequest) {

        Page<Metadata> metadatas = search(indexName, query, pageRequest);

        List<MetadataAndPayload> content = metadatas.getContent().stream()
                .map(m -> MetadataAndPayload.of(m, storageService.read(m.getContentId()))).collect(Collectors.toList());

        return Page.of(pageRequest, content, metadatas.getTotalElements());
    }
}
