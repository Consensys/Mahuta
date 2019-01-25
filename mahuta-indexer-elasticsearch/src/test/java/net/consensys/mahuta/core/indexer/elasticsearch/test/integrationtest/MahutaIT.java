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
import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.IndexingRequestAndMetadata;
import net.consensys.mahuta.core.test.utils.MahutaTestAbstract;
import net.consensys.mahuta.core.utils.FileUtils;

public class MahutaIT extends MahutaTestAbstract {
    
    private static IndexingRequestUtils indexingRequestUtils;
    
    @BeforeClass
    public static void startContainers() throws IOException {
        ContainerUtils.startContainer("ipfs", ContainerType.IPFS);
        ContainerUtils.startContainer("elasticsearch", ContainerType.ELASTICSEARCH);
        
        indexingRequestUtils = new IndexingRequestUtils(new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    private String indexName;
    
    public MahutaIT () {
        super(ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name")), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
        
    }  
    
    @Before
    public void before() {
        indexName = mockNeat.strings().size(20).get();
        indexingService.createIndex(indexName, FileUtils.readFileInputStream("index_mapping.json"));
        
    }
    
    @Test
    public void indexInputStream() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomInputStreamIndexingRequest(indexName);
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexByteArray() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomByteArrayIndexingRequest(indexName);
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexString() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest(indexName);
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexCid() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.index(requestAndMetadata);
    }
    
    @Test
    public void deindex() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.deindex(requestAndMetadata);
    }
    
    @Test
    public void getById() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.getById(requestAndMetadata);
    }
    
    @Test
    public void getByHash() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        super.getByHash(requestAndMetadata);
    }
    
    @Test
    public void searchAll() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        super.searchAll(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 3);
    }
    
    @Test
    public void searchWithEqualsFilter() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomCIDIndexingRequest(indexName);

        super.search(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 
                    Query.newQuery()
                        .contains(AUTHOR_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(AUTHOR_FIELD))
                        .contains(TITLE_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(TITLE_FIELD))
                        .equals(IS_PUBLISHED_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(IS_PUBLISHED_FIELD))
                        .equals(DATE_CREATED_FIELD, ((Date)requestAndMetadata2.getRequest().getIndexFields().get(DATE_CREATED_FIELD)).getTime())
                        .equals(VIEWS_FIELD, requestAndMetadata2.getRequest().getIndexFields().get(VIEWS_FIELD)), 
                     1,  requestAndMetadata2);
    }
    
    @Test
    public void searchWithFullTextFilter1() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Aurelie Legay");

        super.search(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "Jeanmart"), 2,  requestAndMetadata2);
    }
    
    @Test
    public void searchWithFullTextFilter2() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Aurelie Legay");

        super.search(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "LEGaY"), 1,  requestAndMetadata3);
    }
    
    @Test
    public void searchWithFullTextFilter3() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata1 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Gregoire Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata2 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Isabelle Jeanmart");
        IndexingRequestAndMetadata requestAndMetadata3 = indexingRequestUtils.generateRandomStringIndexingRequest(indexName,null,  AUTHOR_FIELD, "Aurelie Legay");

        super.search(Arrays.asList(requestAndMetadata1, requestAndMetadata2, requestAndMetadata3), 
                Query.newQuery().fullText(AUTHOR_FIELD, "Greg"), 1,  requestAndMetadata1);
    }
}
