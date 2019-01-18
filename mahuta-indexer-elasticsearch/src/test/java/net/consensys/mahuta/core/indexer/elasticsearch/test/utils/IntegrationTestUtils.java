package net.consensys.mahuta.core.indexer.elasticsearch.test.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.andreinc.mockneat.MockNeat;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.InputStreamIndexingRequest;
import net.consensys.mahuta.core.domain.indexing.StringIndexingRequest;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.utils.FileUtils;
import net.consensys.mahuta.core.utils.ValidatorUtils;

public abstract class IntegrationTestUtils {

    public enum Status {DRAFT, PUBLISHED, DELETED}
    
    protected static final String FILE_PATH = "pdf-sample.pdf";
    protected static final InputStream FILE = FileUtils.readFileInputString(FILE_PATH);
    protected static final String FILE_PATH2 = "pdf-sample2.pdf";
    protected static final InputStream FILE2 = FileUtils.readFileInputString(FILE_PATH2);
    protected static final String FILE_HASH = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
    protected static final String FILE_HASH2 = "QmaNxbQNrJdLzzd8CKRutBjMZ6GXRjvuPepLuNSsfdeJRJ";
    protected static final String FILE_TYPE = "application/pdf";
    protected static final String TXT_TYPE = "text/plain";

    protected static final InputStream MAPPING = FileUtils.readFileInputString("index_mapping.json");
    
    protected static final String AUTHOR_FIELD = "author";
    protected static final String TITLE_FIELD = "title";
    protected static final String IS_PUBLISHED_FIELD = "isPublished";
    protected static final String DATE_CREATED_FIELD = "dateCreated";
    protected static final String VIEWS_FIELD = "views";
    protected static final String STATUS_FIELD = "status";
    
    protected static final MockNeat mockNeat = MockNeat.threadLocal();

    protected static GenericContainer ipfs;
    protected static GenericContainer elasticsearch;
    protected static final String CLUSTER_NAME = "docker-cluster";
    
 
    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ipfs = new GenericContainer("jbenet/go-ipfs").withExposedPorts(5001, 4001, 8080);
        ipfs.start();
        
        elasticsearch = new GenericContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.5.4").withExposedPorts(9300);
        elasticsearch.start();
    }
    
    @AfterClass
    public static void stopContainers() {
        ipfs.stop();
        elasticsearch.stop();
    }
    
    
    protected static StorageService getStorageService() {
        return IPFSService
                .connect(ipfs.getContainerIpAddress(), ipfs.getFirstMappedPort());
    }
    
    protected static IndexingService getIndexingService() {
        return getIndexingService(null);
    }
    
    protected static IndexingService getIndexingService(String indexName) {
        return getIndexingService(indexName, null);
    }
    
    protected static IndexingService getIndexingService(String indexName, InputStream mapping) {
        ElasticSearchService elasticSearchService = ElasticSearchService
                .connect(elasticsearch.getContainerIpAddress(), elasticsearch.getFirstMappedPort(), CLUSTER_NAME);
        
        if(!ValidatorUtils.isEmpty(indexName)) {
            elasticSearchService.withIndex(indexName, mapping);
        }
        
        return elasticSearchService;
    }
    
    
    protected static Mahuta get() {
        return new MahutaFactory()
                .configureStorage(getStorageService())
                .configureIndexer(getIndexingService())
                .build();
    }
    
    
    protected static Mahuta get(String indexName, InputStream mapping) {
        return new MahutaFactory()
                .configureStorage(getStorageService())
                .configureIndexer(getIndexingService(indexName, mapping))
                .build();
    }
    
    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName) {
        String indexDocId = mockNeat.strings().size(50).get();

        return generateRandomStringIndexingRequest(indexName, indexDocId);
    }
    
    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId) {
        String contentType = TXT_TYPE;

        return generateRandomStringIndexingRequest(indexName, indexDocId, contentType);
    }
    
    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId, String contentType) {

        return generateRandomStringIndexingRequest(indexName, indexDocId, contentType, 
                mockNeat.names().full().get(), 
                mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(), 
                mockNeat.bools().get(), 
                mockNeat.localDates().toUtilDate().get(), 
                mockNeat.ints().range(100, 10000000).get(),
                mockNeat.from(Status.class).get());
    }
    
    protected static IndexingRequest generateRandomStringIndexingRequest(String indexName, String indexDocId, String contentType, String author, String title, boolean isPublished, Date dateCreated, int views, Status status) {
       
        Map<String, Object> indexFields = Maps.newHashMap();
        indexFields.put(AUTHOR_FIELD, author);
        indexFields.put(TITLE_FIELD, title);
        indexFields.put(IS_PUBLISHED_FIELD, isPublished);
        indexFields.put(DATE_CREATED_FIELD, dateCreated);
        indexFields.put(VIEWS_FIELD, views);
        indexFields.put(STATUS_FIELD, status);
        
        return StringIndexingRequest.build()
                .content(mockNeat.strings().size(10000).get())
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(indexFields);
    }
    
    protected static IndexingRequest generateRandomInputStreamIndexingRequest(String indexName, InputStream file) {
        String indexDocId = mockNeat.strings().size(50).get();
        String contentType = FILE_TYPE;
        
        Map<String, Object> indexFields = ImmutableMap.
                of(AUTHOR_FIELD, mockNeat.names().full().get(),
                   TITLE_FIELD, mockNeat.strings().size(mockNeat.ints().range(10, 100).get()).get(),
                   IS_PUBLISHED_FIELD, mockNeat.bools().get(),
                   DATE_CREATED_FIELD, mockNeat.localDates().toUtilDate().get(),
                   VIEWS_FIELD, mockNeat.ints().range(100, 10000000).get()
                );
        
        return InputStreamIndexingRequest.build()
                .content(file)
                .indexName(indexName)
                .indexDocId(indexDocId)
                .contentType(contentType)
                .indexFields(indexFields);
    }
}
