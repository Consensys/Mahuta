package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.AUTHOR_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.DATE_CREATED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IS_PUBLISHED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.TITLE_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.VIEWS_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;
import net.consensys.mahuta.core.test.utils.TestUtils;
import net.consensys.mahuta.core.utils.FileUtils;

public class ElasticSearchIndexerIT extends TestUtils  {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
        
        indexingRequestUtils = new IndexingRequestUtils(new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    @Test
    public void connection() throws Exception {
        //////////////////////////////
        ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        //////////////////////////////
    }

    @Test(expected = ConnectionException.class)
    public void connectionException() throws Exception {
        //////////////////////////////
        ElasticSearchService.connect("blablabla", ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        //////////////////////////////
    }

    @Test
    public void createIndex() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        //////////////////////////////
        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        //////////////////////////////
    }

    @Test
    public void index() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        String docId = service.index(
                requestAndMetadata.getRequest().getIndexName(), 
                requestAndMetadata.getRequest().getIndexDocId(), 
                requestAndMetadata.getMetadata().getContentId(), 
                requestAndMetadata.getRequest().getContentType(), 
                requestAndMetadata.getRequest().getIndexFields());
        //////////////////////////////

        assertEquals(requestAndMetadata.getRequest().getIndexDocId(), docId);
        ;
    }

    @Test
    public void update() throws Exception {
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest();
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(
                requestAndMetadata1.getRequest().getIndexName(),
                requestAndMetadata1.getRequest().getIndexDocId());

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(requestAndMetadata1.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        

        //////////////////////////////
        String docId1 = service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());

        String docId2 = service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        
        Metadata metadata = service.getDocument(requestAndMetadata1.getRequest().getIndexName(), docId1);
        //////////////////////////////

        assertEquals(docId1, docId2);
        assertTrue(requestAndMetadata2.getRequest().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(requestAndMetadata2.getMetadata().getContentId(), metadata.getContentId());
        assertEquals(requestAndMetadata2.getRequest().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(requestAndMetadata2.getRequest().getContentType(), metadata.getContentType());
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
        ;
    }

//    @Test(expected=NotFoundException.class)
//    public void deindex() throws Exception {
//        String indexName = mockNeat.strings().size(20).get();
//        String contentId = mockNeat.strings().size(40).type(StringType.HEX).get();
//
//        IndexingService service = getIndexingService(indexName);
//        
//        StringIndexingRequest request = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName);
//
//        //////////////////////////////
//        String docId = service.index(indexName, request.getIndexDocId(), contentId, request.getContentType(), request.getIndexFields());
//        service.deindex(indexName, docId);
//        service.getDocument(indexName, docId);
//        //////////////////////////////
//        ;
//    }
//
//    @Test
//    public void findDocument() throws Exception {
//        String indexName = mockNeat.strings().size(20).get();
//        String contentId = mockNeat.strings().size(40).type(StringType.HEX).get();
//
//        IndexingService service = getIndexingService(indexName);
//        
//        StringIndexingRequest request = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName);
//
//        //////////////////////////////
//        String docId = service.index(indexName, request.getIndexDocId(), contentId, request.getContentType(), request.getIndexFields());
//        Metadata metadata = service.getDocument(indexName, docId);
//        //////////////////////////////
//
//        assertTrue(indexName.equalsIgnoreCase(metadata.getIndexName()));
//        assertEquals(contentId, metadata.getContentId());
//        assertEquals(request.getIndexDocId(), metadata.getIndexDocId());
//        assertEquals(request.getContentType(), metadata.getContentType());
//        assertEquals(request.getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
//        assertEquals(request.getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
//        assertEquals(request.getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
//        assertEquals(((Date)request.getIndexFields().get(DATE_CREATED_FIELD)).getTime(), metadata.getIndexFields().get(DATE_CREATED_FIELD));
//        assertEquals(request.getIndexFields().get(DATE_CREATED_FIELD), new Date((Long) metadata.getIndexFields().get(DATE_CREATED_FIELD)));
//        assertEquals(request.getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
//    }
//
//    @Test
//    public void searchAll() throws Exception {
//        Integer pageSize = 10;
//        Integer noDocs = 50;
//        Integer nbPages = 6;
//        
//        String indexName = mockNeat.strings().size(20).get();
//
//        IndexingService service = getIndexingService(indexName);
//        
//
//        //////////////////////////////
//
//        String contentId = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName, String.format("%05d", 0));
//        service.index(indexName, request.getIndexDocId(), contentId, request.getContentType(), request.getIndexFields());
//        
//        IntStream.range(0, noDocs).forEach(i-> {
//            String c = mockNeat.strings().size(40).type(StringType.HEX).get();
//            StringIndexingRequest r = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName, String.format("%05d", i+1));
//            service.index(indexName, r.getIndexDocId(), c, r.getContentType(), r.getIndexFields());
//        });
//        
//        Page<Metadata> result = service.searchDocuments(indexName, null, PageRequest.of(0, pageSize, "_id", SortDirection.ASC));
//        //////////////////////////////
//
//        assertEquals(Integer.valueOf(noDocs+1), result.getTotalElements());
//        assertEquals(nbPages, result.getTotalPages());
//        assertTrue(indexName.equalsIgnoreCase(result.getContent().get(0).getIndexName()));
//        assertEquals(request.getIndexDocId(), result.getContent().get(0).getIndexDocId());
//        assertEquals(contentId, result.getContent().get(0).getContentId());
//        assertEquals(request.getIndexFields().get(AUTHOR_FIELD), result.getContent().get(0).getIndexFields().get(AUTHOR_FIELD));
//        assertEquals(request.getIndexFields().get(TITLE_FIELD), result.getContent().get(0).getIndexFields().get(TITLE_FIELD));
//        assertEquals(request.getIndexFields().get(IS_PUBLISHED_FIELD), result.getContent().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
//        assertEquals(request.getIndexFields().get(DATE_CREATED_FIELD), new Date((Long) result.getContent().get(0).getIndexFields().get(DATE_CREATED_FIELD)));
//        assertEquals(request.getIndexFields().get(VIEWS_FIELD), result.getContent().get(0).getIndexFields().get(VIEWS_FIELD));
//        
//
//    }
//
//    @Test
//    public void searchDocumentsWithEqualsFilter() throws Exception {
//        
//        String indexName = mockNeat.strings().size(20).get();
//
//        IndexingService service = getIndexingService(indexName);
//
//        String contentId1 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request1 = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName);
//        
//        String contentId2 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request2 = (StringIndexingRequest) generateRandomStringIndexingRequest(indexName);
//
//        //////////////////////////////
//        service.index(indexName, request1.getIndexDocId(), contentId1, request1.getContentType(), request1.getIndexFields());
//        service.index(indexName, request2.getIndexDocId(), contentId2, request2.getContentType(), request2.getIndexFields());
//        Page<Metadata> result = service.searchDocuments(
//                indexName, 
//                Query.newQuery()
//                                .contains(AUTHOR_FIELD, request2.getIndexFields().get(AUTHOR_FIELD))
//                                .contains(TITLE_FIELD, request2.getIndexFields().get(TITLE_FIELD))
//                                .equals(IS_PUBLISHED_FIELD, request2.getIndexFields().get(IS_PUBLISHED_FIELD))
//                                .equals(DATE_CREATED_FIELD, ((Date)request2.getIndexFields().get(DATE_CREATED_FIELD)).getTime())
//                                .equals(VIEWS_FIELD, request2.getIndexFields().get(VIEWS_FIELD))
//                                ,
//                PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        //////////////////////////////
//
//        assertEquals(Integer.valueOf(1), result.getTotalElements());
//        assertTrue(indexName.equalsIgnoreCase(result.getContent().get(0).getIndexName()));
//        assertEquals(request2.getIndexDocId(), result.getContent().get(0).getIndexDocId());
//        assertEquals(contentId2, result.getContent().get(0).getContentId());
//        assertEquals(request2.getContentType(), result.getContent().get(0).getContentType());
//        assertEquals(request2.getIndexFields().get(AUTHOR_FIELD), result.getContent().get(0).getIndexFields().get(AUTHOR_FIELD));
//        assertEquals(request2.getIndexFields().get(TITLE_FIELD), result.getContent().get(0).getIndexFields().get(TITLE_FIELD));
//        assertEquals(request2.getIndexFields().get(IS_PUBLISHED_FIELD), result.getContent().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
//        assertEquals(request2.getIndexFields().get(DATE_CREATED_FIELD), new Date((Long) result.getContent().get(0).getIndexFields().get(DATE_CREATED_FIELD)));
//        assertEquals(request2.getIndexFields().get(VIEWS_FIELD), result.getContent().get(0).getIndexFields().get(VIEWS_FIELD));
//
//    }
//
//    @Test
//    public void searchDocumentsWithFulltextFilter() throws Exception {
//        
//        String indexName = mockNeat.strings().size(20).get();
//
//        IndexingService service = getIndexingService(indexName);
//
//        String contentId1 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request1 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                "Gregoire Jeanmart",
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                1, Status.PUBLISHED);
//        
//        String contentId2 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request2 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                "Isabelle Jeanmart",
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                2, Status.PUBLISHED);
//        
//        String contentId3 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request3 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                "Aurelie Legay",
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                3, Status.PUBLISHED);
//
//
//        //////////////////////////////
//        service.index(indexName, request1.getIndexDocId(), contentId1, request1.getContentType(), request1.getIndexFields());
//        service.index(indexName, request2.getIndexDocId(), contentId2, request2.getContentType(), request2.getIndexFields());
//        service.index(indexName, request3.getIndexDocId(), contentId3, request3.getContentType(), request3.getIndexFields());
//        
//        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "Jeanmart"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(2), result1.getTotalElements());
//        
//        Page<Metadata> result2 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "LEGaY"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(1), result2.getTotalElements());
//        
//        Page<Metadata> result3 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "Greg"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(1), result3.getTotalElements());
//
//        
//        //////////////////////////////
//
//
//    }
//
//    @Test
//    public void searchDocumentsWithRangeFilter() throws Exception {
//        
//        String indexName = mockNeat.strings().size(20).get();
//
//        IndexingService service = getIndexingService(indexName);
//
//        String contentId1 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request1 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                1, Status.PUBLISHED);
//        
//        String contentId2 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request2 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                2, Status.PUBLISHED);
//        
//        String contentId3 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request3 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                3, Status.PUBLISHED);
//
//        String contentId4 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request4 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                4, Status.PUBLISHED);
//        
//        String contentId5 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request5 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                5, Status.PUBLISHED);
//
//        //////////////////////////////
//        service.index(indexName, request1.getIndexDocId(), contentId1, request1.getContentType(), request1.getIndexFields());
//        service.index(indexName, request2.getIndexDocId(), contentId2, request2.getContentType(), request2.getIndexFields());
//        service.index(indexName, request3.getIndexDocId(), contentId3, request3.getContentType(), request3.getIndexFields());
//        service.index(indexName, request4.getIndexDocId(), contentId4, request4.getContentType(), request4.getIndexFields());
//        service.index(indexName, request5.getIndexDocId(), contentId5, request5.getContentType(), request5.getIndexFields());
//        
//        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().lessThan(VIEWS_FIELD, 3), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(2), result1.getTotalElements());
//        
//        Page<Metadata> result2 = service.searchDocuments(indexName, Query.newQuery().lessThanOrEquals(VIEWS_FIELD, 3), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(3), result2.getTotalElements());
//        
//        Page<Metadata> result3 = service.searchDocuments(indexName, Query.newQuery().greaterThan(VIEWS_FIELD, 4), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(1), result3.getTotalElements());
//        
//        Page<Metadata> result4 = service.searchDocuments(indexName, Query.newQuery().greaterThanOrEquals(VIEWS_FIELD, 5), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(1), result4.getTotalElements());
//        
//        Page<Metadata> result5 = service.searchDocuments(indexName, Query.newQuery()
//                .greaterThanOrEquals(VIEWS_FIELD, 2)
//                .lessThan(VIEWS_FIELD, 4), 
//                PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(2), result5.getTotalElements());
//        
//        //////////////////////////////
//
//
//    }
//
//    @Test
//    public void searchDocumentsWithInFilter() throws Exception {
//        
//        String indexName = mockNeat.strings().size(20).get();
//
//        IndexingService service = getIndexingService(indexName);
//
//        String contentId1 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request1 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                1, Status.DRAFT);
//        
//        String contentId2 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request2 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                2, Status.DRAFT);
//        
//        String contentId3 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request3 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                3, Status.PUBLISHED);
//
//        String contentId4 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request4 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                4, Status.PUBLISHED);
//        
//        String contentId5 = mockNeat.strings().size(40).type(StringType.HEX).get();
//        StringIndexingRequest request5 = (StringIndexingRequest) generateRandomStringIndexingRequest(
//                indexName, 
//                mockNeat.strings().get(),
//                TXT_TYPE,
//                mockNeat.names().full().get(),
//                mockNeat.strings().get(), 
//                true,
//                mockNeat.localDates().toUtilDate().get(),
//                5, Status.DELETED);
//
//        //////////////////////////////
//        service.index(indexName, request1.getIndexDocId(), contentId1, request1.getContentType(), request1.getIndexFields());
//        service.index(indexName, request2.getIndexDocId(), contentId2, request2.getContentType(), request2.getIndexFields());
//        service.index(indexName, request3.getIndexDocId(), contentId3, request3.getContentType(), request3.getIndexFields());
//        service.index(indexName, request4.getIndexDocId(), contentId4, request4.getContentType(), request4.getIndexFields());
//        service.index(indexName, request5.getIndexDocId(), contentId5, request5.getContentType(), request5.getIndexFields());
//        
//        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().in(STATUS_FIELD, Arrays.asList(Status.DRAFT, Status.PUBLISHED)), PageRequest.of(0, 10, "_id", SortDirection.ASC));
//        assertEquals(Integer.valueOf(4), result1.getTotalElements());
//        
//        
//        //////////////////////////////
//
//
//    }

}
