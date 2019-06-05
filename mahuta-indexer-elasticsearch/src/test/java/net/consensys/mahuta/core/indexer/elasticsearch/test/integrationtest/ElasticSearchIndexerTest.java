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
import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest.SortDirection;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.NoIndexException;
import net.consensys.mahuta.core.exception.NotFoundException;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.Status;
import net.consensys.mahuta.core.test.utils.TestUtils;
import net.consensys.mahuta.core.utils.BytesUtils;

public class ElasticSearchIndexerTest extends TestUtils  {

    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    public ElasticSearchIndexerTest () {
        indexingRequestUtils = new IndexingRequestUtils(new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
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
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
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

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(builderAndResponse.getBuilder().getRequest().getIndexName(), BytesUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        //////////////////////////////

        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexDocId(), docId);
    }

    @Test
    public void indexWithIdContent() throws Exception {

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(true);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(builderAndResponse.getBuilder().getRequest().getIndexName(), BytesUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getResponse().getContent(),
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        //////////////////////////////

        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexDocId(), docId);
    }

    @Test
    public void indexWithoutId() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, null);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        //////////////////////////////

        assertNotNull(docId);;
    }
    
    @Test(expected=NoIndexException.class)
    public void indexWithNoIndex() throws Exception {

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        
        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        //////////////////////////////
    }


    @Test
    public void update() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(50).get();
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        

        //////////////////////////////
        String docId1 = service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());

        String docId2 = service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        
        Metadata metadata = service.getDocument(builderAndResponse1.getBuilder().getRequest().getIndexName(), docId1);
        //////////////////////////////

        assertEquals(docId1, docId2);
        assertTrue(builderAndResponse2.getBuilder().getRequest().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(builderAndResponse2.getResponse().getContentId(), metadata.getContentId());
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(builderAndResponse2.getBuilder().getRequest().getContentType(), metadata.getContentType());
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }


    @Test
    public void updateField() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        String indexDocId = mockNeat.strings().size(50).get();
        String titleNewValue = "test";
              
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, indexDocId);

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        

        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());

        service.updateField(indexName, indexDocId, TITLE_FIELD, titleNewValue);
        
        Metadata metadata = service.getDocument(builderAndResponse.getBuilder().getRequest().getIndexName(), docId);
        //////////////////////////////

        assertEquals(indexDocId, docId);
        assertEquals(titleNewValue, metadata.getIndexFields().get(TITLE_FIELD));
    }

    @Test(expected=NotFoundException.class)
    public void deindex() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(builderAndResponse.getBuilder().getRequest().getIndexName(), BytesUtils.readFileInputStream("index_mapping.json"));
        
        
        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());

        service.deindex(builderAndResponse.getBuilder().getRequest().getIndexName(), docId);
        
        service.getDocument(builderAndResponse.getBuilder().getRequest().getIndexName(), docId);
        //////////////////////////////
    }

    @Test
    public void findDocument() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(builderAndResponse.getBuilder().getRequest().getIndexName(), BytesUtils.readFileInputStream("index_mapping.json"));
        

        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        
        Metadata metadata = service.getDocument(builderAndResponse.getBuilder().getRequest().getIndexName(), docId);
        //////////////////////////////

        assertTrue(builderAndResponse.getBuilder().getRequest().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(builderAndResponse.getResponse().getContentId(), metadata.getContentId());
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(builderAndResponse.getBuilder().getRequest().getContentType(), metadata.getContentType());
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }
    
    @Test
    public void findDocumentWithContent() throws Exception {
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(builderAndResponse.getBuilder().getRequest().getIndexName(), BytesUtils.readFileInputStream("index_mapping.json"));
        

        //////////////////////////////
        String docId = service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getResponse().getContent(),
                builderAndResponse.getBuilder().getRequest().getIndexFields());
        
        Metadata metadata = service.getDocument(builderAndResponse.getBuilder().getRequest().getIndexName(), docId);
        //////////////////////////////

        assertTrue(builderAndResponse.getBuilder().getRequest().getIndexName().equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(builderAndResponse.getResponse().getContentId(), metadata.getContentId());
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexDocId(), metadata.getIndexDocId());
        assertEquals(builderAndResponse.getResponse().getContent(), metadata.getContent());
        assertEquals(builderAndResponse.getBuilder().getRequest().getContentType(), metadata.getContentType());
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }

    @Test
    public void searchAll() throws Exception {
        String indexName = mockNeat.strings().size(20).get();
        Integer pageSize = 10;
        Integer noDocs = 50;
        Integer nbPages = 6;
        
        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
        //////////////////////////////
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, String.format("%05d", 0));
        service.index(
                builderAndResponse.getBuilder().getRequest().getIndexName(), 
                builderAndResponse.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse.getResponse().getContentId(), 
                builderAndResponse.getBuilder().getRequest().getContentType(), 
                builderAndResponse.getBuilder().getRequest().getIndexFields());

        IntStream.range(0, noDocs).forEach(i-> {
            BuilderAndResponse<IndexingRequest, IndexingResponse> x = indexingRequestUtils.generateRandomStringIndexingRequest(indexName, String.format("%05d", i+1));
            service.index(
                    x.getBuilder().getRequest().getIndexName(), 
                    x.getBuilder().getRequest().getIndexDocId(), 
                    x.getResponse().getContentId(), 
                    x.getBuilder().getRequest().getContentType(), 
                    x.getBuilder().getRequest().getIndexFields());
        });
        
        Page<Metadata> result = service.searchDocuments(indexName, null, PageRequest.of(0, pageSize, "_id", SortDirection.ASC));
        //////////////////////////////

        assertEquals(Integer.valueOf(noDocs+1), result.getTotalElements());
        assertEquals(nbPages, result.getTotalPages());
        assertTrue(indexName.equalsIgnoreCase(result.getElements().get(0).getIndexName()));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexDocId(), result.getElements().get(0).getIndexDocId());
        assertEquals(builderAndResponse.getResponse().getContentId(), result.getElements().get(0).getContentId());
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD), result.getElements().get(0).getIndexFields().get(AUTHOR_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD), result.getElements().get(0).getIndexFields().get(TITLE_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), result.getElements().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD), result.getElements().get(0).getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(builderAndResponse.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD), result.getElements().get(0).getIndexFields().get(VIEWS_FIELD));
        

    }

    @Test
    public void searchDocumentsWithEqualsFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);

        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        
        Page<Metadata> result = service.searchDocuments(
                indexName, 
                Query.newQuery()
                                .contains(AUTHOR_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD))
                                .contains(TITLE_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD))
                                .equals(IS_PUBLISHED_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD))
                                .equals(DATE_CREATED_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD))
                                .equals(VIEWS_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD))
                                ,
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        //////////////////////////////

        assertEquals(Integer.valueOf(1), result.getTotalElements());
        assertTrue(indexName.equalsIgnoreCase(result.getElements().get(0).getIndexName()));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexDocId(), result.getElements().get(0).getIndexDocId());
        assertEquals(builderAndResponse2.getResponse().getContentId(), result.getElements().get(0).getContentId());
        assertEquals(builderAndResponse2.getBuilder().getRequest().getContentType(), result.getElements().get(0).getContentType());
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD), result.getElements().get(0).getIndexFields().get(AUTHOR_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD), result.getElements().get(0).getIndexFields().get(TITLE_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD), result.getElements().get(0).getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD), result.getElements().get(0).getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(builderAndResponse2.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD), result.getElements().get(0).getIndexFields().get(VIEWS_FIELD));

    }

    @Test
    public void searchDocumentsWithFulltextFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Aurelie Legay");

        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        
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
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 1);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 2);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 3);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse4 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 4);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse5 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  VIEWS_FIELD, 5);
        
        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse4.getBuilder().getRequest().getIndexName(), 
                builderAndResponse4.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse4.getResponse().getContentId(), 
                builderAndResponse4.getBuilder().getRequest().getContentType(), 
                builderAndResponse4.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse5.getBuilder().getRequest().getIndexName(), 
                builderAndResponse5.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse5.getResponse().getContentId(), 
                builderAndResponse5.getBuilder().getRequest().getContentType(), 
                builderAndResponse5.getBuilder().getRequest().getIndexFields());
        
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
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse4 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse5 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);

        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse4.getBuilder().getRequest().getIndexName(), 
                builderAndResponse4.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse4.getResponse().getContentId(), 
                builderAndResponse4.getBuilder().getRequest().getContentType(), 
                builderAndResponse4.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse5.getBuilder().getRequest().getIndexName(), 
                builderAndResponse5.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse5.getResponse().getContentId(), 
                builderAndResponse5.getBuilder().getRequest().getContentType(), 
                builderAndResponse5.getBuilder().getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().in(STATUS_FIELD, Arrays.asList(Status.DRAFT, Status.PUBLISHED)), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(4), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithNotEqualsFilter() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, Query.newQuery().notEquals(STATUS_FIELD, Status.DRAFT.toString()), PageRequest.of(0, 10, "_id", SortDirection.ASC));
        assertEquals(Integer.valueOf(1), result1.getTotalElements());
        //////////////////////////////
    }

    @Test
    public void searchDocumentsWithOr() throws Exception {
        
        String indexName = mockNeat.strings().size(20).get();

        IndexingService service = ElasticSearchService
                .connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"))
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DRAFT);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        
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
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"))
                .configureIndexNullValue(true);
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, null);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);

        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        
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
                .withIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"))
                .configureIndexNullValue(false);
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, null);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.DELETED);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  STATUS_FIELD, Status.PUBLISHED);
        
        //////////////////////////////
        service.index(
                builderAndResponse1.getBuilder().getRequest().getIndexName(), 
                builderAndResponse1.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse1.getResponse().getContentId(), 
                builderAndResponse1.getBuilder().getRequest().getContentType(), 
                builderAndResponse1.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse2.getBuilder().getRequest().getIndexName(), 
                builderAndResponse2.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse2.getResponse().getContentId(), 
                builderAndResponse2.getBuilder().getRequest().getContentType(), 
                builderAndResponse2.getBuilder().getRequest().getIndexFields());
        service.index(
                builderAndResponse3.getBuilder().getRequest().getIndexName(), 
                builderAndResponse3.getBuilder().getRequest().getIndexDocId(), 
                builderAndResponse3.getResponse().getContentId(), 
                builderAndResponse3.getBuilder().getRequest().getContentType(), 
                builderAndResponse3.getBuilder().getRequest().getIndexFields());
        
        Page<Metadata> result1 = service.searchDocuments(indexName, 
                Query.newQuery().equals(STATUS_FIELD, null), 
                PageRequest.of(0, 10, "_id", SortDirection.ASC));
        
        assertEquals(Integer.valueOf(3), result1.getTotalElements());
        //////////////////////////////
    }

}
