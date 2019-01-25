package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.AUTHOR_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.DATE_CREATED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IS_PUBLISHED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.STATUS_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.TITLE_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.VIEWS_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;
import net.consensys.mahuta.core.domain.common.PageRequest.SortDirection;
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.Status;
import net.consensys.mahuta.core.test.utils.TestUtils;
import net.consensys.mahuta.core.utils.FileUtils;

public class ElasticSearchIndexerTest extends TestUtils  {

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
        ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        //////////////////////////////
    }

    @Test
    public void createIndex2() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        
        //////////////////////////////
        service.createIndex(indexName);
        //////////////////////////////
    }

    @Test
    public void createIndexAlreadyExist() throws Exception {
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        
        //////////////////////////////
        service.createIndex(indexName);
        service.createIndex(indexName);
        //////////////////////////////
    }

    @Test
    public void indexWithId() throws Exception {
        
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
    }

    @Test
    public void indexWithoutId() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, null);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        String docId = service.index(
                requestAndMetadata.getRequest().getIndexName(), 
                requestAndMetadata.getRequest().getIndexDocId(), 
                requestAndMetadata.getMetadata().getContentId(), 
                requestAndMetadata.getRequest().getContentType(), 
                requestAndMetadata.getRequest().getIndexFields());
        //////////////////////////////

        assertNotNull(docId);;
    }

    @Test
    public void update() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(50).get();
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        

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
    }

    @Test(expected=NotFoundException.class)
    public void deindex() throws Exception {
        
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

        service.deindex(requestAndMetadata.getRequest().getIndexName(), docId);
        
        service.getDocument(requestAndMetadata.getRequest().getIndexName(), docId);
        //////////////////////////////
    }

    @Test
    public void findDocument() throws Exception {
        
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
        
        Metadata metadata = service.getDocument(requestAndMetadata.getRequest().getIndexName(), docId);
        //////////////////////////////

        assertTrue(requestAndMetadata.getRequest().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(requestAndMetadata.getMetadata().getContentId(), metadata.getContentId());
        assertEquals(requestAndMetadata.getRequest().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(requestAndMetadata.getRequest().getContentType(), metadata.getContentType());
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }

    @Test
    public void searchAll() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        Integer pageSize = 10;
        Integer noDocs = 50;
        Integer nbPages = 6;
        
        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, String.format("%05d", 0));
        service.index(
                requestAndMetadata.getRequest().getIndexName(), 
                requestAndMetadata.getRequest().getIndexDocId(), 
                requestAndMetadata.getMetadata().getContentId(), 
                requestAndMetadata.getRequest().getContentType(), 
                requestAndMetadata.getRequest().getIndexFields());

        IntStream.range(0, noDocs).forEach(i-> {
            IndexingRequestAndMetadata x = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, String.format("%05d", i+1));
            service.index(
                    x.getRequest().getIndexName(), 
                    x.getRequest().getIndexDocId(), 
                    x.getMetadata().getContentId(), 
                    x.getRequest().getContentType(), 
                    x.getRequest().getIndexFields());
        });
        
        Page<Metadata> result = service.searchDocuments(indexName, null, PageRequest.of(0, pageSize, "_id", SortDirection.ASC));
        //////////////////////////////

        assertEquals(Integer.valueOf(noDocs+1), result.getTotalElements());
        assertEquals(nbPages, result.getTotalPages());
        assertTrue(indexName.equalsIgnoreCase(result.getElements().get(0).getIndexName()));
        assertEquals(requestAndMetadata.getRequest().getIndexDocId(), result.getElements().get(0).getIndexDocId());
        assertEquals(requestAndMetadata.getMetadata().getContentId(), result.getElements().get(0).getContentId());
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(AUTHOR_FIELD), result.getElements().get(0).getIndexFields().get(AUTHOR_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(TITLE_FIELD), result.getElements().get(0).getIndexFields().get(TITLE_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), result.getElements().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(DATE_CREATED_FIELD), result.getElements().get(0).getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(requestAndMetadata.getRequest().getIndexFields().get(VIEWS_FIELD), result.getElements().get(0).getIndexFields().get(VIEWS_FIELD));
        

    }

    @Test
    public void searchDocumentsWithEqualsFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);

        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        
        Page<Metadata> result = service.searchDocuments(
                indexName, 
                Query.newQuery()
                                .contains(AUTHOR_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(AUTHOR_FIELD))
                                .contains(TITLE_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(TITLE_FIELD))
                                .equals(IS_PUBLISHED_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD))
                                .equals(DATE_CREATED_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(DATE_CREATED_FIELD))
                                .equals(VIEWS_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(VIEWS_FIELD))
                                ,
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        //////////////////////////////

        assertEquals(Integer.valueOf(1), result.getTotalElements());
        assertTrue(indexName.equalsIgnoreCase(result.getElements().get(0).getIndexName()));
        assertEquals(requestAndMetadata2.getRequest().getIndexDocId(), result.getElements().get(0).getIndexDocId());
        assertEquals(requestAndMetadata2.getMetadata().getContentId(), result.getElements().get(0).getContentId());
        assertEquals(requestAndMetadata2.getRequest().getContentType(), result.getElements().get(0).getContentType());
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(AUTHOR_FIELD), result.getElements().get(0).getIndexFields().get(AUTHOR_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(TITLE_FIELD), result.getElements().get(0).getIndexFields().get(TITLE_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), result.getElements().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(DATE_CREATED_FIELD), result.getElements().get(0).getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(requestAndMetadata2.getRequest().getIndexFields().get(VIEWS_FIELD), result.getElements().get(0).getIndexFields().get(VIEWS_FIELD));

    }

    @Test
    public void searchDocumentsWithFulltextFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Aurelie Legay");

        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "Jeanmart"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(2), result1.getTotalElements());
        
        Page<Metadata> result2 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "LEGaY"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result2.getTotalElements());
        
        Page<Metadata> result3 = service.searchDocuments(indexName, Query.newQuery().fullText(AUTHOR_FIELD, "Greg"), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result3.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithRangeFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));

        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 1);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 2);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 3);
        IndexingRequestAndMetadata requestAndMetadata4 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 4);
        IndexingRequestAndMetadata requestAndMetadata5 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 5);
        
        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        service.index(
                requestAndMetadata4.getRequest().getIndexName(), 
                requestAndMetadata4.getRequest().getIndexDocId(), 
                requestAndMetadata4.getMetadata().getContentId(), 
                requestAndMetadata4.getRequest().getContentType(), 
                requestAndMetadata4.getRequest().getIndexFields());
        service.index(
                requestAndMetadata5.getRequest().getIndexName(), 
                requestAndMetadata5.getRequest().getIndexDocId(), 
                requestAndMetadata5.getMetadata().getContentId(), 
                requestAndMetadata5.getRequest().getContentType(), 
                requestAndMetadata5.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().lessThan(VIEWS_FIELD, 3), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(2), result1.getTotalElements());
        
        Page<Metadata> result2 = service.searchDocuments(indexName, Query.newQuery().lessThanOrEquals(VIEWS_FIELD, 3), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(3), result2.getTotalElements());
        
        Page<Metadata> result3 = service.searchDocuments(indexName, Query.newQuery().greaterThan(VIEWS_FIELD, 4), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result3.getTotalElements());
        
        Page<Metadata> result4 = service.searchDocuments(indexName, Query.newQuery().greaterThanOrEquals(VIEWS_FIELD, 5), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result4.getTotalElements());
        
        Page<Metadata> result5 = service.searchDocuments(indexName, Query.newQuery()
                .greaterThanOrEquals(VIEWS_FIELD, 2)
                .lessThan(VIEWS_FIELD, 4), 
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(2), result5.getTotalElements());
        
        //////////////////////////////


    }

    @Test
    public void searchDocumentsWithInFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        IndexingRequestAndMetadata requestAndMetadata4 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        IndexingRequestAndMetadata requestAndMetadata5 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);

        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        service.index(
                requestAndMetadata4.getRequest().getIndexName(), 
                requestAndMetadata4.getRequest().getIndexDocId(), 
                requestAndMetadata4.getMetadata().getContentId(), 
                requestAndMetadata4.getRequest().getContentType(), 
                requestAndMetadata4.getRequest().getIndexFields());
        service.index(
                requestAndMetadata5.getRequest().getIndexName(), 
                requestAndMetadata5.getRequest().getIndexDocId(), 
                requestAndMetadata5.getMetadata().getContentId(), 
                requestAndMetadata5.getRequest().getContentType(), 
                requestAndMetadata5.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().in(STATUS_FIELD, Arrays.asList(Status.DRAFT, Status.PUBLISHED)), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(4), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithNotEqualsFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));

        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().notEquals(STATUS_FIELD, Status.DRAFT.toString()), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithOr() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        

        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, 
                Query.newQuery()
                    .or(Query.newQuery().equals(STATUS_FIELD, Status.DRAFT.toString()))
                    .or(Query.newQuery().equals(STATUS_FIELD, Status.PUBLISHED.toString())), 
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        
        assertEquals(Integer.valueOf(2), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithNullFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"))
                .configureIndexNullValue(true);
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, null);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);

        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, 
                Query.newQuery().equals(STATUS_FIELD, null), 
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        
        assertEquals(Integer.valueOf(1), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithNotNullFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"))
                .configureIndexNullValue(false);
        
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, null);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                requestAndMetadata1.getRequest().getIndexName(), 
                requestAndMetadata1.getRequest().getIndexDocId(), 
                requestAndMetadata1.getMetadata().getContentId(), 
                requestAndMetadata1.getRequest().getContentType(), 
                requestAndMetadata1.getRequest().getIndexFields());
        service.index(
                requestAndMetadata2.getRequest().getIndexName(), 
                requestAndMetadata2.getRequest().getIndexDocId(), 
                requestAndMetadata2.getMetadata().getContentId(), 
                requestAndMetadata2.getRequest().getContentType(), 
                requestAndMetadata2.getRequest().getIndexFields());
        service.index(
                requestAndMetadata3.getRequest().getIndexName(), 
                requestAndMetadata3.getRequest().getIndexDocId(), 
                requestAndMetadata3.getMetadata().getContentId(), 
                requestAndMetadata3.getRequest().getContentType(), 
                requestAndMetadata3.getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, 
                Query.newQuery().equals(STATUS_FIELD, null), 
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        
        assertEquals(Integer.valueOf(3), result1.getTotalElements());
        //////////////////////////////
    }

}
