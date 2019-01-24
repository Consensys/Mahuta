package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.ipfs.api.IPFS;
import net.andreinc.mockneat.types.enums.StringType;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
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
    
    public MahutaIT () {
        super(ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name")), 
              IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"))
        );
    }
    
    @Test
    public void indexInputStream() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomInputStreamIndexingRequest();
        indexingService.createIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexByteArray() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomByteArrayIndexingRequest();
        indexingService.createIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexString() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomStringIndexingRequest();
        indexingService.createIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        super.index(requestAndMetadata);
    }
    
    @Test
    public void indexCid() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();
        indexingService.createIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        super.index(requestAndMetadata);
    }
    
    @Test
    public void deindex() throws Exception {
        IndexingRequestAndMetadata requestAndMetadata = indexingRequestUtils.generateRandomCIDIndexingRequest();
        indexingService.createIndex(requestAndMetadata.getRequest().getIndexName(), FileUtils.readFileInputStream("index_mapping.json"));
        super.deindex(requestAndMetadata);
    }

}
