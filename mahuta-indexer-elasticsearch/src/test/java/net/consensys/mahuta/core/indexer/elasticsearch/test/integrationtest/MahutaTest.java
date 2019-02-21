package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.AUTHOR_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.DATE_CREATED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IS_PUBLISHED_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.TITLE_FIELD;
import static net.consensys.mahuta.core.test.utils.IndexingRequestUtils.VIEWS_FIELD;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import net.consensys.mahuta.core.domain.common.query.Query;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.MahutaServiceImpl;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.FileUtils;

public class MahutaTest extends MahutaTestAbstract {
    
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
    
    public MahutaTest () {
        super(ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name")), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
        indexingRequestUtils = new IndexingRequestUtils(new MahutaServiceImpl(storageService, indexingService), 
                new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
        
    }  
    
    @Before
    public void before() {
        indexName = mockNeat.strings().size(20).get();
        indexingService.createIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
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
}
