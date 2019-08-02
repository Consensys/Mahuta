package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.AUTHOR_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.DATE_CREATED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IS_PUBLISHED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.TITLE_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.VIEWS_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.Response.ResponseStatus;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.get.GetResponse;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.DefaultMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.BytesUtils;

public class DefaultMahutaTest extends MahutaTestAbstract {
    
    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    private String indexName;
    
    public DefaultMahutaTest () {
        super(ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name")), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
        indexingRequestUtils = new IndexingRequestUtils(new DefaultMahutaService(storageService, indexingService), 
                new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
        
    }  
    
    @Before
    public void before() {
        indexName = mockNeat.strings().size(20).get();
        indexingService.createIndex(indexName, BytesUtils.readFileInputStream("index_mapping.json"));
        
    }
    
    @Test
    public void connectViaTransportClient() throws Exception {

        String host = ContainerUtils.getHost("elasticsearch");
        Integer port = ContainerUtils.getPort("elasticsearch");
        String clusterName = ContainerUtils.getConfig("elasticsearch", "cluster-name");
                
        PreBuiltTransportClient pbtc = new PreBuiltTransportClient(
                Settings.builder().put("cluster.name", clusterName).build());
        TransportClient transportClient = pbtc
                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
        
        IndexingService esService = ElasticSearchService.connect(transportClient).withIndex("test");
        
        assertNotNull( esService.getIndexes());
    }
    
    @Test
    public void createIndex() throws Exception {
        indexName = mockNeat.strings().size(20).get();
        super.creatIndex(indexName);
    }
    
    @Test
    public void indexInputStream() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomInputStreamIndexingRequest(indexName);
        super.index(builderAndResponse);
    }
    
    @Test
    public void indexString() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
        super.index(builderAndResponse);
    }
    
    @Test
    public void indexCid() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.index(builderAndResponse);
    }
    
    @Test
    public void deindex() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.deindex(builderAndResponse);
    }
    
    @Test
    public void getById() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.getById(builderAndResponse);
    }
    
    @Test
    public void getByIdIndexContent() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        builderAndResponse.getBuilder().getRequest().setIndexContent(true);
        super.getById(builderAndResponse);
    }
    
    @Test
    public void getByHash() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.getByHash(builderAndResponse);
    }
    
    @Test
    public void searchAll() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        super.searchAll(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 3);
    }
    
    @Test
    public void searchWithEqualsFilter() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        super.search(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 
                    Query.newQuery()
                        .contains(AUTHOR_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(AUTHOR_FIELD))
                        .contains(TITLE_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(TITLE_FIELD))
                        .equals(IS_PUBLISHED_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(IS_PUBLISHED_FIELD))
                        .equals(DATE_CREATED_FIELD, ((Date)builderAndResponse2.getBuilder().getRequest().getIndexFields().get(DATE_CREATED_FIELD)).getTime())
                        .equals(VIEWS_FIELD, builderAndResponse2.getBuilder().getRequest().getIndexFields().get(VIEWS_FIELD)), 
                     1,  builderAndResponse2);
    }
    
    @Test
    public void searchWithFullTextFilter1() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "D Legay");

        super.search(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "Jeanmart"), 2);
    }
    
    @Test
    public void searchWithFullTextFilter2() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "D Legay");

        super.search(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "LEGaY"), 1,  builderAndResponse3);
    }
    
    @Test
    public void searchWithFullTextFilter3() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "D Legay");

        super.search(Arrays.asList(builderAndResponse1, builderAndResponse2, builderAndResponse3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "Greg"), 1,  builderAndResponse1);
    }
    
    @Test
    public void updateField() {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
        
        super.updateField(builderAndResponse, AUTHOR_FIELD, "bob markey");
    }
    
    @Test
    public void checkPinned() throws Exception {
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        
        ////////////////////////
        IndexingResponse indexingResponse = builderAndResponse.getBuilder().execute();
        assertEquals(ResponseStatus.SUCCESS, indexingResponse.getStatus());
        
        Thread.sleep(1000);
        
        GetResponse getResponse = mahuta.prepareGet()
                .indexName(builderAndResponse.getBuilder().getRequest().getIndexName())
                .indexDocId(builderAndResponse.getBuilder().getRequest().getIndexDocId())
                .loadFile(true)
                .execute();
        assertEquals(ResponseStatus.SUCCESS, getResponse.getStatus());
        ////////////////////////

        assertTrue(getResponse.getMetadata().isPinned());
    }
}
