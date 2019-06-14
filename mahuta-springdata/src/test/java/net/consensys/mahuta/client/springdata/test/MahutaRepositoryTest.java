package net.consensys.mahuta.client.springdata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.collect.ImmutableMap;

import io.ipfs.api.IPFS;
import lombok.extern.slf4j.Slf4j;
import net.andreinc.mockneat.MockNeat;
import net.consensys.mahuta.client.springdata.test.sample.Entity;
import net.consensys.mahuta.client.springdata.test.sample.Factory;
import net.consensys.mahuta.client.springdata.test.sample.TestRepository;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.domain.indexing.IndexingResponse;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.DefaultMahutaService;
import net.consensys.mahuta.core.service.indexing.IndexingService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils;
import net.consensys.mahuta.core.test.utils.IndexingRequestUtils.BuilderAndResponse;

@Slf4j
public class MahutaRepositoryTest {

    private static final MockNeat mockNeat = MockNeat.threadLocal();

    private TestRepository underTest;
    private final String indexName = "entity";
    private Mahuta mahuta;
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
    
    public MahutaRepositoryTest() {
        
        final IndexingService indexingService = ElasticSearchService.connect(ContainerUtils.getHost("elasticsearch"), ContainerUtils.getPort("elasticsearch"), ContainerUtils.getConfig("elasticsearch", "cluster-name"));
        final StorageService storageService = IPFSService.connect(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs"));
        
        mahuta = new MahutaFactory()
                .configureIndexer(indexingService)
                .configureStorage(storageService)
                .defaultImplementation();
        
        underTest = new TestRepository(mahuta);
        
        indexingRequestUtils = new IndexingRequestUtils(new DefaultMahutaService(storageService, indexingService), 
                new IPFS(ContainerUtils.getHost("ipfs"), ContainerUtils.getPort("ipfs")));
    }

    @After
    public void clean() throws UnknownHostException {
        underTest.findAll().forEach(e->underTest.deleteById(e.getId()));
    }


    @Test
    public void save() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateStringIndexingRequest(
                entity.toJSON(), indexName, id, entity.toMap());
        
        // #################################################
        Entity entitySaved = underTest.save(entity);
        // #################################################

        log.debug(entitySaved.toString());

        assertEquals(entity.getName(), entitySaved.getName());
        assertEquals(entity.getAge(), entitySaved.getAge());        
        assertEquals(entity.getTags().size(), entitySaved.getTags().size());
        assertEquals(id, entitySaved.getId());
        assertEquals(builderAndResponse.getResponse().getContentId(), entitySaved.getHash());
    }


    @Test
    public void saveNoId() throws Exception {
        Entity entity = Factory.getEntity();
        
        // #################################################
        Entity entitySaved = underTest.save(entity);
        // #################################################

        log.debug(entitySaved.toString());

        assertEquals(entity.getName(), entitySaved.getName());
        assertEquals(entity.getAge(), entitySaved.getAge());        
        assertEquals(entity.getTags().size(), entitySaved.getTags().size());
        assertNotNull(entitySaved.getId());
        //assertEquals(builderAndResponse.getResponse().getContentId(), entitySaved.getHash());
    }
    
    @Test
    public void saveWithoutAutosetup() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);
        
        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateStringIndexingRequest(
                entity.toJSON(), indexName, id, entity.toMap());
        
        // #################################################
        String hashReturned = underTest.saveNoIndexation(entity);
        // #################################################

        assertEquals(builderAndResponse.getResponse().getContentId(), hashReturned);
    }

    @Test
    public void findOne() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateStringIndexingRequest(
                entity.toJSON(), indexName, id, entity.toMap());
        
        // #################################################
        underTest.save(entity);
        Optional<Entity> entityFetched = underTest.findById(id);
        // #################################################

        assertTrue(entityFetched.isPresent());
        assertEquals(entity.getName(), entityFetched.get().getName());
        assertEquals(entity.getAge(), entityFetched.get().getAge());
        assertEquals(entity.getId(), entityFetched.get().getId());
        assertEquals(builderAndResponse.getResponse().getContentId(), entityFetched.get().getHash());
    }


    @Test
    public void findOneNotFoundException() throws Exception {
        
        // #################################################
        Optional<Entity> entityFetched = underTest.findById("unknownsfdsfds");
        // #################################################

        assertFalse(entityFetched.isPresent());
    }
    
    @Test
    public void findAll() throws Exception {
        final int no = 50;

        IntStream.range(0, no).forEach(i -> {
            Entity entity = Factory.getEntity(mockNeat.strings().size(50).get());
            underTest.save(entity);
        });

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll();
        // #################################################

        assertEquals(20, result.getContent().size());
        assertEquals(no, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }
    
    @Test
    public void findAllSort() throws Exception {
        final int no = 3;

        IntStream.range(0, no).forEach(i -> {
            Entity entity = Factory.getEntity(mockNeat.strings().size(50).get());
            underTest.save(entity);
        });

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll(Sort.by(Direction.ASC, "name"));
        // #################################################

        assertEquals(3, result.getContent().size());
        assertEquals(no, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    public void findAllWithPagination() throws Exception {
        final int no = 8;

        IntStream.range(0, no).forEach(i -> {
            Entity entity = Factory.getEntity(mockNeat.strings().size(50).get());
            underTest.save(entity);
        });

        // #################################################
        Page<Entity> result1 = (Page<Entity>) underTest.findAll(PageRequest.of(0, 5));
        Page<Entity> result2 = (Page<Entity>) underTest.findAll(PageRequest.of(1, 5));
        // #################################################

        assertEquals(5, result1.getContent().size());
        assertEquals(3, result2.getContent().size());
        assertEquals(no, result1.getTotalElements());
        assertEquals(no, result2.getTotalElements());
        assertEquals(2, result1.getTotalPages());
        assertEquals(2, result2.getTotalPages());
    }

    @Test
    public void findAllWithSort() throws Exception {
        final int no = 5;

        IntStream.range(0, no).forEach(i -> {
            Entity entity = Factory.getEntity(String.valueOf(i));
            underTest.save(entity);
        });

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll(PageRequest.of(0, 5, Direction.DESC, "_id"));
        // #################################################

        assertEquals(5, result.getContent().size());
        assertEquals(no, result.getTotalElements());
        assertEquals(String.valueOf(no-1), result.getContent().get(0).getId());
    }

    @Test
    public void exists() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);
        
        // #################################################
        underTest.save(entity);
        boolean exist = underTest.existsById(id);
        // #################################################
        
        assertTrue(exist);
    }


    @Test
    public void doesNotExist() throws Exception {
        // #################################################
        boolean exist = underTest.existsById("unknownnnnn");
        // #################################################
        
        assertFalse(exist);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void saveIterable() throws Exception {
        // #################################################
        underTest.saveAll(Arrays.asList(Factory.getEntity()));
        // #################################################
    }

    @Test
    public void delete() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);
        
        // #################################################
        underTest.save(entity);
        
        boolean exist1 = underTest.existsById(id);
        assertTrue(exist1);
        // #################################################
        
        // #################################################
        underTest.deleteById(id);
        boolean exist2 = underTest.existsById(id);
        assertFalse(exist2);
        
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteIterable() throws Exception {
        // #################################################
        underTest.deleteAll(Arrays.asList(Factory.getEntity()));
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteAll() throws Exception {
        // #################################################
        underTest.deleteAll();
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void count() throws Exception {
        // #################################################
        underTest.count();
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void findAllIterable() throws Exception {
        List<String> ids = new ArrayList<>();
        // #################################################
        underTest.findAllById(ids);
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteEntiy() throws Exception {
        // #################################################
        underTest.delete(Factory.getEntity());
        // #################################################
    }

    @Test
    public void findByfullTextSearch() throws Exception {
        
        String id1 = mockNeat.strings().size(50).get();
        Entity entity1 = Factory.getEntity(id1, "Gregoire Jeanmart", 31);
        underTest.save(entity1);
        
        String id2 = mockNeat.strings().size(50).get();
        Entity entity2 = Factory.getEntity(id2, "Isabelle Jeanmart", 30);
        underTest.save(entity2);
        
        String id3 = mockNeat.strings().size(50).get();
        Entity entity3 = Factory.getEntity(id3, "Bob Dylan", 30);
        underTest.save(entity3);
        
        // #################################################
        Page<Entity> result = underTest.findByfullTextSearch("Jeanmart", PageRequest.of(0, 5, Direction.ASC, "name"));
        // #################################################

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(id1, result.getContent().get(0).getId());
    }

    @Test
    public void findByfullTextSearchNull() throws Exception {
        
        String id1 = mockNeat.strings().size(50).get();
        Entity entity1 = Factory.getEntity(id1, "Gregoire Jeanmart", 31);
        underTest.save(entity1);
        
        String id2 = mockNeat.strings().size(50).get();
        Entity entity2 = Factory.getEntity(id2, "Isabelle Jeanmart", 30);
        underTest.save(entity2);
        
        String id3 = mockNeat.strings().size(50).get();
        Entity entity3 = Factory.getEntity(id3, "Bob Dylan", 30);
        underTest.save(entity3);
        
        // #################################################
        Page<Entity> result = underTest.findByfullTextSearch(null, PageRequest.of(0, 5, Direction.ASC, "name"));
        // #################################################

        assertNull(result);
    }

    @Test
    public void findByHash() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateStringIndexingRequest(
                entity.toJSON(), indexName, id, entity.toMap());
        
        // #################################################
        underTest.save(entity);
        Optional<Entity> result = underTest.findByHash(builderAndResponse.getResponse().getContentId());
        // #################################################
 
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    public void findByHashNotFound() throws Exception {
        // #################################################
        Optional<Entity> result = underTest.findByHash("QmVPKMdfLaEsMSB3aZUrbGbZ4TnUobo2TGbkeXZbLBAmbn");
        // #################################################
 
        assertFalse(result.isPresent());
    }    
    
    @Test
    public void updateField() throws Exception {
        String id = mockNeat.strings().size(50).get();
        Entity entity = Factory.getEntity(id);
        Map<String, Object> externalField = ImmutableMap.of("attribute1", "value");

        BuilderAndResponse<IndexingRequest, IndexingResponse> builderAndResponse = indexingRequestUtils.generateStringIndexingRequest(
                entity.toJSON(), indexName, id, entity.toMap());
        
        // #################################################
        underTest.save(entity, externalField);
        underTest.updateIndexField(id, "attribute1", "edit");
        Page<Entity> result1 = underTest.findByAttribute("attribute1", "value", PageRequest.of(0, 10));
        Page<Entity> result2 = underTest.findByAttribute("attribute1", "edit", PageRequest.of(0, 10));
        // #################################################

        assertEquals(0, result1.getTotalElements());
        assertEquals(1, result2.getTotalElements());
    }
}