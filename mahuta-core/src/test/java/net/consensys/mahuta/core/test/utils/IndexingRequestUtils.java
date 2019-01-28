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
    private final boolean dateTimestamp;
    
    public IndexingRequestUtils(IPFS ipfs) {
        this(ipfs, false);
    }
    public IndexingRequestUtils(IPFS ipfs, boolean dateTimestamp) {
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
        
        return this.generateRandomCIDIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomCIDIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomCIDIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomCIDIndexingRequest(indexName, indexDocId, fields);
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
        
        return this.generateRandomInputStreamIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomInputStreamIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomInputStreamIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomInputStreamIndexingRequest(indexName, indexDocId, fields);
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
        
        return this.generateRandomByteArrayIndexingRequest(indexName);
    }
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();
        
        return this.generateRandomByteArrayIndexingRequest(indexName, indexDocId);
    }
    
    public IndexingRequestAndMetadata generateRandomByteArrayIndexingRequest(String indexName, String indexDocId) {
        Map<String, Object> fields = generateRamdomFields();
        
        return this.generateRandomByteArrayIndexingRequest(indexName, indexDocId, fields);
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
