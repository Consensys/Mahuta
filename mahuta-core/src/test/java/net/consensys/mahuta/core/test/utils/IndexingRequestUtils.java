package net.consensys.mahuta.core.test.utils;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.andreinc.mockneat.MockNeat;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.indexing.CIDIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;

public class IndexingRequestUtils {

    protected static final MockNeat mockNeat = MockNeat.threadLocal();
    
    public enum Status {DRAFT, PUBLISHED, DELETED}
    public static final String AUTHOR_FIELD = "author";
    public static final String TITLE_FIELD = "title";
    public static final String IS_PUBLISHED_FIELD = "isPublished";
    public static final String DATE_CREATED_FIELD = "dateCreated";
    public static final String VIEWS_FIELD = "views";
    public static final String STATUS_FIELD = "status";
    
    public static Map<String, Object> generateRamdomFields() {
        return ImmutableMap.
                of(AUTHOR_FIELD, mockNeat.names().full().get(),
                   TITLE_FIELD, mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(),
                   IS_PUBLISHED_FIELD, mockNeat.bools().get(),
                   DATE_CREATED_FIELD, mockNeat.localDates().toUtilDate().get(),
                   VIEWS_FIELD, mockNeat.ints().range(100, 10000000).get()
                );
    }
    
    public static IndexingRequest generateRandomInputStreamIndexingRequest() {

        String indexName = mockNeat.strings().size(50).get();
        String indexDocId = mockNeat.strings().size(50).get();
        FileInfo file = mockNeat.from(FileTestUtils.files).get();
        String contentId = file.getCid();
        String contentType = file.getType();
        Map<String, Object> fields = generateRamdomFields();
        
        

        return InputStreamIndexingRequest.build()
                .content(file.getIs())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(fields);
    }
    
//    
//    
//    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId) {
//        String contentType = ConstantUtils.TEXT_SAMPLE_TYPE;
//
//        return generateRandomStringIndexingRequest(indexName, indexDocId, contentType);
//    }
//    
//    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId, String contentType) {
//
//        return generateRandomStringIndexingRequest(indexName, indexDocId, contentType, 
//                mockNeat.names().full().get(), 
//                mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(), 
//                mockNeat.bools().get(), 
//                mockNeat.localDates().toUtilDate().get(), 
//                mockNeat.ints().range(100, 10000000).get(),
//                mockNeat.from(Status.class).get());
//    }
//    
//    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId, String contentType, String author, String title, boolean isPublished, Date dateCreated, int views, Status status) {
//       
//        Map<String, Object> indexFields = Maps.newHashMap();
//        indexFields.put(AUTHOR_FIELD, author);
//        indexFields.put(TITLE_FIELD, title);
//        indexFields.put(IS_PUBLISHED_FIELD, isPublished);
//        indexFields.put(DATE_CREATED_FIELD, dateCreated);
//        indexFields.put(VIEWS_FIELD, views);
//        indexFields.put(STATUS_FIELD, status);
//        
//        return StringIndexingRequest.build()
//                .content(mockNeat.strings().size(10000).get())
//                .indexName(indexName)
//                .indexDocId(indexDocId)
//                .contentType(contentType)
//                .indexFields(indexFields);
//    }
//    
//    protected static IndexingRequest generateRandomInputStreamIndexingRequest(String indexName, InputStream file) {
//        
//        return generateRandomInputStreamIndexingRequest(indexName, file, mockNeat.strings().size(50).get());
//    }
//    
//    protected static IndexingRequest generateRandomInputStreamIndexingRequest(String indexName, InputStream file, String indexDocId) {
//
//        String contentType = ConstantUtils.FILE_TYPE;
//        
//        Map<String, Object> indexFields = ImmutableMap.
//                of(AUTHOR_FIELD, mockNeat.names().full().get(),
//                   TITLE_FIELD, mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(),
//                   IS_PUBLISHED_FIELD, mockNeat.bools().get(),
//                   DATE_CREATED_FIELD, mockNeat.localDates().toUtilDate().get(),
//                   VIEWS_FIELD, mockNeat.ints().range(100, 10000000).get()
//                );
//        
//        return InputStreamIndexingRequest.build()
//                .content(file)
//                .indexName(indexName)
//                .indexDocId(indexDocId)
//                .contentType(contentType)
//                .indexFields(indexFields);
//    }
//    
//    protected static IndexingRequest generateRandomCIDIndexingRequest(String indexName, String cid, String indexDocId) {
//
//        String contentType = ConstantUtils.TEXT_SAMPLE_TYPE;
//        
//        Map<String, Object> indexFields = ImmutableMap.
//                of(AUTHOR_FIELD, mockNeat.names().full().get(),
//                   TITLE_FIELD, mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(),
//                   IS_PUBLISHED_FIELD, mockNeat.bools().get(),
//                   DATE_CREATED_FIELD, mockNeat.localDates().toUtilDate().get(),
//                   VIEWS_FIELD, mockNeat.ints().range(100, 10000000).get()
//                );
//        
//        return CIDIndexingRequest.build()
//                .content(cid)
//                .indexName(indexName)
//                .indexDocId(indexDocId)
//                .contentType(contentType)
//                .indexFields(indexFields);
//    }
//    
    static class IndexingRequestAndMetadata {
        private @Getter IndexingRequest request;
        private @Getter Metadata response;
        
        public IndexingRequestAndMetadata(IndexingRequest request, Metadata response) {
            this.request = request;
            this.response = response;
        }
    }
    
}
