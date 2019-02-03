package net.consensys.mahuta.core.test.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.ipfs.api.IPFS;
import lombok.Getter;
import net.consensys.mahuta.core.domain.Builder;
import net.consensys.mahuta.core.domain.Request;
import net.consensys.mahuta.core.domain.Response;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequestBuilder;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequestBuilder;
import net.consensys.mahuta.core.service.MahutaService;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;

public class IndexingRequestUtils extends TestUtils{

    
    public enum Status {DRAFT, PUBLISHED, DELETED}
    public static final String AUTHOR_FIELD = "author";
    public static final String TITLE_FIELD = "title";
    public static final String IS_PUBLISHED_FIELD = "isPublished";
    public static final String DATE_CREATED_FIELD = "dateCreated";
    public static final String VIEWS_FIELD = "views";
    public static final String STATUS_FIELD = "status";

    private final MahutaService service;
    private final IPFS ipfs;
    private final boolean dateTimestamp;

    public IndexingRequestUtils(IPFS ipfs) {
        this(null, ipfs);
    }
    public IndexingRequestUtils(MahutaService service, IPFS ipfs) {
        this(service, ipfs, false);
    }
    public IndexingRequestUtils(MahutaService service, IPFS ipfs, boolean dateTimestamp) {
        this.service = service;
        this.ipfs = ipfs;
        this.dateTimestamp = dateTimestamp;
    }
        
    public Map<String, Object> generateRamdomFields() {
        
        return generateFields(
                mockNeat.names().full().get(), 
                mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(), 
                mockNeat.bools().get(), 
                mockNeat.localDates().thisYear().toUtilDate().get(), 
                mockNeat.ints().range(100, 10000000).get(), 
                mockNeat.from(Status.class).get());
    }    
    
    public Map<String, Object> generateFields(String author, String title, boolean isPublished, Date dateCreaed, int views, Status status) {
        
        Map<String, Object> fields = new HashMap<>();
        fields.put(AUTHOR_FIELD, author);
        fields.put(TITLE_FIELD, title);
        fields.put(IS_PUBLISHED_FIELD, isPublished);
        fields.put(DATE_CREATED_FIELD, date(dateCreaed));
        fields.put(VIEWS_FIELD, views);
        fields.put(STATUS_FIELD, Optional.ofNullable(status).map(Object::toString).orElse(null)); //enum are complex to manage
        
        return fields;
    }
    
    public Map<String, Object> generateFields(String field, Object value) {
        
        Map<String, Object> fields = generateRamdomFields();
        
        if(fields.containsKey(field)) {
            fields.put(field, date(value));
        }

        return fields;
    }
    
    public Object date(Object value) {
        if(value instanceof Date && dateTimestamp) {
            return ((Date) value).getTime();
        }
        return value;
    }
    

    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateStringIndexingRequest(String content, String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newPlainText(ipfs, content);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        Builder<IndexingRequest, IndexingResponse> builder = new StringIndexingRequestBuilder(service)
                .content(new String(file.getBytearray()))
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        IndexingResponse response = IndexingResponse.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new BuilderAndResponse<>(builder, response);
    }
    
    
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomStringIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomStringIndexingRequest(indexName);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomStringIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomStringIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomStringIndexingRequest(String indexName, String indexDocId, String field, Object value) {
        Map<String, Object> fields = generateFields(field, value);
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomStringIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        Builder<IndexingRequest, IndexingResponse> builder = new StringIndexingRequestBuilder(service)
                .content(new String(file.getBytearray()))
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        IndexingResponse response = IndexingResponse.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new BuilderAndResponse<>(builder, response);
    }
    
    /////////////////////////////
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomCIDIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomCIDIndexingRequest(indexName);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomCIDIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomCIDIndexingRequest(indexName, indexDocId);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomCIDIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomCIDIndexingRequest(indexName, indexDocId, fields);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomCIDIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        Builder<IndexingRequest, IndexingResponse> builder = new CIDIndexingRequestBuilder(service)
                .cid(file.getCid())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        IndexingResponse response = IndexingResponse.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new BuilderAndResponse<>(builder, response);
    }
    
    /////////////////////////////
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomInputStreamIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomInputStreamIndexingRequest(indexName);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomInputStreamIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomInputStreamIndexingRequest(indexName, indexDocId);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomInputStreamIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomInputStreamIndexingRequest(indexName, indexDocId, fields);
    }
    
    public BuilderAndResponse<IndexingRequest, IndexingResponse> generateRandomInputStreamIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        Builder<IndexingRequest, IndexingResponse> builder = new InputStreamIndexingRequestBuilder(service)
                .content(file.getIs())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        IndexingResponse response = IndexingResponse.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new BuilderAndResponse<>(builder, response);
    }

      
    public static class BuilderAndResponse<R extends Request, S extends Response> {
        private @Getter Builder<R, S> builder;
        private @Getter S response;
        
        public BuilderAndResponse(Builder<R, S> builder, S response) {
            this.builder = builder;
            this.response = response;
        }
    }
    
}
