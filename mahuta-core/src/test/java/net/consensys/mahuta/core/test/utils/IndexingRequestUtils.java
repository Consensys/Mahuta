package net.consensys.mahuta.core.test.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.ipfs.api.IPFS;
import lombok.Getter;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.ByteArrayIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;

public class IndexingRequestUtils extends TestUtils{

    
    public enum Status {DRAFT, PUBLISHED, DELETED}
    public static final String AUTHOR_FIELD = "author";
    public static final String TITLE_FIELD = "title";
    public static final String IS_PUBLISHED_FIELD = "isPublished";
    public static final String DATE_CREATED_FIELD = "dateCreated";
    public static final String VIEWS_FIELD = "views";
    public static final String STATUS_FIELD = "status";
    
    private final IPFS ipfs;
    public IndexingRequestUtils(IPFS ipfs) {
        this.ipfs = ipfs;
    }
        
    public static Map<String, Object> generateRamdomFields() {
        
        return generateFields(
                mockNeat.names().full().get(), 
                mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(), 
                mockNeat.bools().get(), 
                mockNeat.localDates().toUtilDate().get(), 
                mockNeat.ints().range(100, 10000000).get(), 
                mockNeat.from(Status.class).get());
    }    
    
    public static Map<String, Object> generateFields(String author, String title, boolean isPublished, Date dateCreaed, int views, Status status) {
        
        Map<String, Object> fields = new HashMap<>();
        fields.put(AUTHOR_FIELD, author);
        fields.put(TITLE_FIELD, title);
        fields.put(IS_PUBLISHED_FIELD, isPublished);
        fields.put(DATE_CREATED_FIELD, dateCreaed);
        fields.put(VIEWS_FIELD, views);
        fields.put(STATUS_FIELD, Optional.ofNullable(status).map(Object::toString).orElse(null)); //enum are complex to manage
        
        return fields;
    }
    
    public static Map<String, Object> generateFields(String field, Object value) {
        
        Map<String, Object> fields = new HashMap<>();
        fields.put(AUTHOR_FIELD, (field != null && field.equals(AUTHOR_FIELD) ? value : mockNeat.names().full().get()));
        fields.put(TITLE_FIELD, (field != null && field.equals(TITLE_FIELD) ? value : mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get()));
        fields.put(IS_PUBLISHED_FIELD, (field != null && field.equals(IS_PUBLISHED_FIELD) ? value : mockNeat.bools().get()));
        fields.put(DATE_CREATED_FIELD, (field != null && field.equals(DATE_CREATED_FIELD) ? value : mockNeat.localDates().toUtilDate().get()));
        fields.put(VIEWS_FIELD, (field != null && field.equals(VIEWS_FIELD) ? value : mockNeat.ints().range(100, 10000000).get()));
        fields.put(STATUS_FIELD, Optional.ofNullable((field != null && field.equals(STATUS_FIELD) ? value : mockNeat.from(Status.class).get())).map(Object::toString).orElse(null)); //enum are complex to manage
        
        return fields;
    }
    
    
    
    public IndexingRequestAndMetadata generateRandomStringIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomStringIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomStringIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomStringIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public IndexingRequestAndMetadata generateRandomStringIndexingRequest(String indexName, String indexDocId, String field, Object value) {
        Map<String, Object> fields = generateFields(field, value);
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public IndexingRequestAndMetadata generateRandomStringIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        IndexingRequest request =  StringIndexingRequest.build()
                .content(new String(file.getBytearray()))
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        Metadata metadata = Metadata.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new IndexingRequestAndMetadata(request, metadata);
    }
    
    /////////////////////////////
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomStringIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        IndexingRequest request =  CIDIndexingRequest.build()
                .content(contentId)
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        Metadata metadata = Metadata.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new IndexingRequestAndMetadata(request, metadata);
    }
    
    /////////////////////////////
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomStringIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        IndexingRequest request =  InputStreamIndexingRequest.build()
                .content(file.getIs())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        Metadata metadata = Metadata.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new IndexingRequestAndMetadata(request, metadata);
    }
    
    /////////////////////////////
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest() {
        String indexName = mockNeat.strings().size(20).get();
        
        return this.generateRandomStringIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomStringIndexingRequest(indexName, indexDocId, fields);
    }
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest(String indexName, String indexDocId, Map<String, Object> fields) {

        FileInfo file = FileTestUtils.newRandomPlainText(ipfs);
        String contentId = file.getCid();
        String contentType = file.getType();
        
        IndexingRequest request =  ByteArrayIndexingRequest.build()
                .content(file.getBytearray())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
        
        Metadata metadata = Metadata.of(indexName, indexDocId, contentId, contentType, fields);
        
        return new IndexingRequestAndMetadata(request, metadata);
    }
    
    
    
    
    
    
    
      
    public static class IndexingRequestAndMetadata {
        private @Getter IndexingRequest request;
        private @Getter Metadata metadata;
        
        public IndexingRequestAndMetadata(IndexingRequest request, Metadata metadata) {
            this.request = request;
            this.metadata = metadata;
        }
    }
    
}
