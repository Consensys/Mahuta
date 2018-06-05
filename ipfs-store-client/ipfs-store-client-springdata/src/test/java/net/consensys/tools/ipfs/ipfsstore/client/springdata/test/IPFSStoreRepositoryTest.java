package net.consensys.tools.ipfs.ipfsstore.client.springdata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.IPFSStoreRepository;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.Entity;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.Factory;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.TestRepository;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;


@RunWith(SpringJUnit4ClassRunner.class)
public class IPFSStoreRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreRepositoryTest.class);
    private static final String CONTENT_TYPE = "application/json";

    private IPFSStoreRepository<Entity, String> underTest;
    private final ObjectMapper MAPPER = new ObjectMapper();

    @MockBean
    private IPFSStore client;

    private String index;
//    private Set<String> externalIndexFields;

    @Before
    public void setup() {
        index = "test";

        Set<String> indexFields = new HashSet<>();
        indexFields.add("id");
        indexFields.add("name");
        indexFields.add("age");

//        externalIndexFields = new HashSet<String>();
//        externalIndexFields.add("content");

        underTest = new TestRepository(client, index, indexFields, Entity.class);
    }


    @Test
    public void save() throws Exception {
        Entity entity = Factory.getEntity();
        String hash = "ABC";

        Mockito.when(client.index(any(InputStream.class), eq(index), anyString(), eq(CONTENT_TYPE), any(Map.class))).thenReturn(hash);

        // #################################################
        Entity entitySaved = underTest.save(entity);
        // #################################################

        LOGGER.debug(entitySaved.toString());

        assertEquals("Entity Name should be " + entity.getName(), entity.getName(), entitySaved.getName());
        assertEquals("Entity Age should be " + entity.getAge(), entity.getAge(), entitySaved.getAge());
        assertNotNull("Entity ID shouldn't be null", entitySaved.getId());

        Mockito.verify(client, Mockito.times(1)).index(any(InputStream.class), eq(index), anyString(), eq(CONTENT_TYPE), any(Map.class));
    }

    @Test
    public void saveWithID() throws Exception {
        String id = "123";
        Entity entity = Factory.getEntity(id);
        String hash = "ABC";

        Mockito.when(client.index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class))).thenReturn(hash);

        // #################################################
        Entity entitySaved = underTest.save(entity);
        // #################################################

        LOGGER.debug(entitySaved.toString());

        assertEquals("Entity Name should be " + entity.getName(), entity.getName(), entitySaved.getName());
        assertEquals("Entity Age should be " + entity.getAge(), entity.getAge(), entitySaved.getAge());
        assertEquals("Entity ID should be " + id, id, entitySaved.getId());

        Mockito.verify(client, Mockito.times(1)).index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class));
    }

    @Test
    public void saveException() throws Exception {
        String id = "123";
        Entity entity = Factory.getEntity(id);

        Mockito.when(client.index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class))).thenThrow(new IPFSStoreException());

        // #################################################
        Entity entitySaved = underTest.save(entity);
        // #################################################

        assertNull("Entity should be null", entitySaved);

        Mockito.verify(client, Mockito.times(1)).index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class));
    }

    @Test
    public void saveWithoutAutosetup() throws Exception {
        Entity entity = Factory.getEntity();
        String hash = "ABC";

        Mockito.when(client.store(any(InputStream.class))).thenReturn(hash);

        // #################################################
        String hashReturned = underTest.saveWithoutAutoSetup(entity);
        // #################################################

        assertEquals(hashReturned, hash);

        Mockito.verify(client, Mockito.times(1)).store(any(InputStream.class));
    }

    @Test
    public void findOne() throws Exception {
        Entity entity = Factory.getEntity(Factory.ID);

        Mockito.when(client.getById(eq(index), eq(Factory.ID))).thenReturn(MAPPER.writeValueAsBytes(entity));

        // #################################################
        Entity entityFetched = underTest.findOne(Factory.ID);
        // #################################################

        LOGGER.debug(entityFetched.toString());

        assertEquals("Entity Name should be " + entity.getName(), entity.getName(), entityFetched.getName());
        assertEquals("Entity Age should be " + entity.getAge(), entity.getAge(), entityFetched.getAge());
        assertEquals("Entity ID should be " + entity.getId(), entity.getId(), entityFetched.getId());

        Mockito.verify(client, Mockito.times(1)).getById(eq(index), eq(Factory.ID));
    }


    @Test
    public void findOneException() throws Exception {
        Mockito.when(client.getById(eq(index), eq(Factory.ID))).thenThrow(new IPFSStoreException("error"));

        // #################################################
        Entity entityFetched = underTest.findOne(Factory.ID);
        // #################################################

        assertNull("Entity should be null", entityFetched);

        Mockito.verify(client, Mockito.times(1)).getById(eq(index), eq(Factory.ID));
    }

    @Test
    public void findAll() throws Exception {
        int total = 50;
        Pageable pagination = new PageRequest(IPFSStoreRepository.DEFAULT_PAGE_NO, IPFSStoreRepository.DEFAULT_PAGE_SIZE);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<byte[]> contentList = page.getContent().stream().map(e -> {
            try {
                return MAPPER.writeValueAsBytes(e);
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<byte[]> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), eq(null), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll();
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + IPFSStoreRepository.DEFAULT_PAGE_SIZE + " elements", IPFSStoreRepository.DEFAULT_PAGE_SIZE, result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + IPFSStoreRepository.DEFAULT_PAGE_NO, IPFSStoreRepository.DEFAULT_PAGE_NO, result.getNumber());

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }

    @Test
    public void findAllWithPagination() throws Exception {
        int total = 10;
        Pageable pagination = new PageRequest(0, 5);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<byte[]> contentList = page.getContent().stream().map(e -> {
            try {
                return MAPPER.writeValueAsBytes(e);
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<byte[]> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), eq(null), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = underTest.findAll(pagination);
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + pagination.getPageSize() + " elements", pagination.getPageSize(), result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + pagination.getPageNumber(), pagination.getPageNumber(), result.getNumber());

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }

    @Test
    public void findAllWithSort() throws Exception {
        int total = 50;
        Sort sort = new Sort(Direction.ASC, "id");
        Pageable pagination = new PageRequest(IPFSStoreRepository.DEFAULT_PAGE_NO, IPFSStoreRepository.DEFAULT_PAGE_SIZE, sort);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<byte[]> contentList = page.getContent().stream().map(e -> {
            try {
                return MAPPER.writeValueAsBytes(e);
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<byte[]> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), eq(null), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll(sort);
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + IPFSStoreRepository.DEFAULT_PAGE_SIZE + " elements", IPFSStoreRepository.DEFAULT_PAGE_SIZE, result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + IPFSStoreRepository.DEFAULT_PAGE_NO, IPFSStoreRepository.DEFAULT_PAGE_NO, result.getNumber());


        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }

    @Test
    public void findAllException() throws Exception {
        Pageable pagination = new PageRequest(0, 5);

        Mockito.when(client.searchAndFetch(eq(index), any(Query.class), eq(pagination))).thenThrow(new IPFSStoreException(new Exception("error")));

        // #################################################
        Page<Entity> result = underTest.findAll(pagination);
        // #################################################

        assertNull("result should be null", result);

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }


    @Test
    public void exists() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        Metadata metadata = new Metadata();
        metadata.setDocumentId(Factory.ID);
        metadata.setHash(hash);
        metadata.setContentType(CONTENT_TYPE);
        metadata.setIndex(index);

        Mockito.when(client.getMetadataById(eq(index), eq(Factory.ID))).thenReturn(metadata);

        // #################################################
        boolean exist = underTest.exists(Factory.ID);
        // #################################################

        assertEquals(true, exist);

        Mockito.verify(client, Mockito.times(1)).getMetadataById(eq(index), eq(Factory.ID));
        // #################################################
    }


    @Test
    public void doesNotExist() throws Exception {

        Mockito.when(client.getMetadataById(eq(index), eq(Factory.ID))).thenReturn(null);

        // #################################################
        boolean exist = underTest.exists(Factory.ID);
        // #################################################

        assertEquals(false, exist);

        Mockito.verify(client, Mockito.times(1)).getMetadataById(eq(index), eq(Factory.ID));
        // #################################################
    }


    @Test
    public void existsExeption() throws Exception {


        Mockito.when(client.getMetadataById(eq(index), eq(Factory.ID))).thenThrow(new IPFSStoreException());

        // #################################################
        underTest.exists(Factory.ID);
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void saveIterable() throws Exception {
        // #################################################
        underTest.save(Factory.getEntities(5));
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void delete() throws Exception {
        // #################################################
        underTest.delete("123");
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteIterable() throws Exception {
        // #################################################
        underTest.delete(Factory.getEntities(5));
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
        underTest.findAll(ids);
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
        int total = 10;
        Pageable pagination = new PageRequest(0, 5);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<byte[]> contentList = page.getContent().stream().map(e -> {
            try {
                return MAPPER.writeValueAsBytes(e);
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<byte[]> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), any(Query.class), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = underTest.findByfullTextSearch("search ", pagination);
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + pagination.getPageSize() + " elements", pagination.getPageSize(), result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + pagination.getPageNumber(), pagination.getPageNumber(), result.getNumber());

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), any(Query.class), eq(pagination));
    }

    @Test
    public void findByfullTextSearchException() throws Exception {
        Pageable pagination = new PageRequest(0, 5);

        Mockito.when(client.searchAndFetch(eq(index), any(Query.class), eq(pagination))).thenThrow(new IPFSStoreException("error", new Exception()));

        // #################################################
        Page<Entity> result = underTest.findByfullTextSearch("search ", pagination);
        // #################################################

        assertNull("result should be null", result);

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), any(Query.class), eq(pagination));
    }


    @Test
    public void findByHash() throws Exception {
        String hash = "ABC";
        Entity entity = Factory.getEntity();

        Mockito.when(client.get(eq(index), (eq(hash)))).thenReturn(MAPPER.writeValueAsBytes(entity));

        // #################################################
        Entity result = underTest.findByHash(hash);
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result Name should be " + entity.getName(), entity.getName(), result.getName());

        Mockito.verify(client, Mockito.times(1)).get(eq(index), eq(hash));
    }
}