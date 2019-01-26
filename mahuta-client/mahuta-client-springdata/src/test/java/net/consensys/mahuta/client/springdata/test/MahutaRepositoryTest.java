package net.consensys.mahuta.client.springdata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import net.consensys.mahuta.client.java.MahutaClient;
import net.consensys.mahuta.client.java.model.IdAndHash;
import net.consensys.mahuta.client.java.model.MetadataAndPayload;
import net.consensys.mahuta.client.springdata.MahutaRepository;
import net.consensys.mahuta.client.springdata.impl.MahutaCustomRepositoryImpl;
import net.consensys.mahuta.client.springdata.test.sample.Entity;
import net.consensys.mahuta.client.springdata.test.sample.Factory;
import net.consensys.mahuta.client.springdata.test.sample.TestRepository;
import net.consensys.mahuta.dto.Metadata;
import net.consensys.mahuta.dto.query.Query;
import net.consensys.mahuta.exception.MahutaException;
import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.exception.TechnicalException;


@RunWith(SpringJUnit4ClassRunner.class)
public class MahutaRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MahutaRepositoryTest.class);
    private static final String CONTENT_TYPE = "application/json";

    private MahutaRepository<Entity, String> underTest;
    private final ObjectMapper MAPPER = new ObjectMapper();

    @MockBean
    private MahutaClient client;

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
        String id = "sdfsdfsdf";

        Mockito.when(client.index(any(InputStream.class), eq(index), anyString(), eq(CONTENT_TYPE), any(Map.class))).thenReturn(IdAndHash.builder().id(id).hash(hash).build());

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

        Mockito.when(client.index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class))).thenReturn(IdAndHash.builder().id(id).hash(hash).build());

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

        Mockito.when(client.index(any(InputStream.class), eq(index), eq(id), eq(CONTENT_TYPE), any(Map.class))).thenThrow(new MahutaException());

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
        String hash = "hash";
        Metadata metadata = new Metadata(index, Factory.ID, hash, null, null);
        Entity entity = Factory.getEntity(Factory.ID);

        Mockito.when(client.getById(eq(index), eq(Factory.ID))).thenReturn(MetadataAndPayload.builder().metadata(metadata).payload(MAPPER.writeValueAsBytes(entity)).build());

        // #################################################
        Entity entityFetched = underTest.findById(Factory.ID).get();
        // #################################################

        LOGGER.debug(entityFetched.toString());

        assertEquals("Entity Name should be " + entity.getName(), entity.getName(), entityFetched.getName());
        assertEquals("Entity Age should be " + entity.getAge(), entity.getAge(), entityFetched.getAge());
        assertEquals("Entity ID should be " + entity.getId(), entity.getId(), entityFetched.getId());

        Mockito.verify(client, Mockito.times(1)).getById(eq(index), eq(Factory.ID));
    }


    @Test(expected=TechnicalException.class)
    public void findOneException() throws Exception {
        Mockito.when(client.getById(eq(index), eq(Factory.ID))).thenThrow(new MahutaException("error"));

        // #################################################
        Optional<Entity> entityFetched = underTest.findById(Factory.ID);
        // #################################################
    }


    @Test
    public void findOneNotFoundException() throws Exception {
        Mockito.when(client.getById(eq(index), eq(Factory.ID))).thenThrow(new NotFoundException("error"));

        // #################################################
        Optional<Entity> entityFetched = underTest.findById(Factory.ID);
        // #################################################

        assertEquals("Entity should not be present", false, entityFetched.isPresent());

        Mockito.verify(client, Mockito.times(1)).getById(eq(index), eq(Factory.ID));
    }
    
    @Test
    public void findAll() throws Exception {
        String hash = "hash";
        int total = 50;
        Pageable pagination = PageRequest.of(MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<MetadataAndPayload> contentList = page.getContent().stream().map(e -> {
            try {
                Metadata m = new Metadata(index, Factory.ID, hash, null, null);
                return MetadataAndPayload.builder().metadata(m).payload(MAPPER.writeValueAsBytes(e)).build();
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<MetadataAndPayload> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), eq(null), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll();
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE + " elements", MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE, result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, result.getNumber());

        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }

    @Test
    public void findAllWithPagination() throws Exception {
        String hash = "h";
        int total = 10;
        Pageable pagination = PageRequest.of(0, 5);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<MetadataAndPayload> contentList = page.getContent().stream().map(e -> {
            try {
                Metadata m = new Metadata(index, Factory.ID, hash, null, null);
                return MetadataAndPayload.builder().metadata(m).payload(MAPPER.writeValueAsBytes(e)).build();
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<MetadataAndPayload> content = new PageImpl<>(contentList, pagination, total);
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
        String hash = "h";
        int total = 50;
        Sort sort = new Sort(Direction.ASC, "id");
        Pageable pagination = PageRequest.of(MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE, sort);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<MetadataAndPayload> contentList = page.getContent().stream().map(e -> {
            try {
                Metadata m = new Metadata(index, Factory.ID, hash, null, null);
                return MetadataAndPayload.builder().metadata(m).payload(MAPPER.writeValueAsBytes(e)).build();
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<MetadataAndPayload> content = new PageImpl<>(contentList, pagination, total);
        Mockito.when(client.searchAndFetch(eq(index), eq(null), eq(pagination))).thenReturn(content);

        // #################################################
        Page<Entity> result = (Page<Entity>) underTest.findAll(sort);
        // #################################################

        LOGGER.debug(result.toString());

        assertEquals("Result should contain " + MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE + " elements", MahutaCustomRepositoryImpl.DEFAULT_PAGE_SIZE, result.getContent().size());
        assertEquals("Result should have a total of " + total + " elements", total, result.getTotalElements());
        assertEquals("Result should have be on page no " + MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, MahutaCustomRepositoryImpl.DEFAULT_PAGE_NO, result.getNumber());


        Mockito.verify(client, Mockito.times(1)).searchAndFetch(eq(index), eq(null), eq(pagination));
    }

    @Test
    public void findAllException() throws Exception {
        Pageable pagination = PageRequest.of(0, 5);

        Mockito.when(client.searchAndFetch(eq(index), isNull(), eq(pagination))).thenThrow(new MahutaException(new Exception("error")));

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
        boolean exist = underTest.existsById(Factory.ID);
        // #################################################

        assertEquals(true, exist);

        Mockito.verify(client, Mockito.times(1)).getMetadataById(eq(index), eq(Factory.ID));
        // #################################################
    }


    @Test
    public void doesNotExist() throws Exception {

        Mockito.when(client.getMetadataById(eq(index), eq(Factory.ID))).thenReturn(null);

        // #################################################
        boolean exist = underTest.existsById(Factory.ID);
        // #################################################

        assertEquals(false, exist);

        Mockito.verify(client, Mockito.times(1)).getMetadataById(eq(index), eq(Factory.ID));
        // #################################################
    }


    @Test
    public void existsExeption() throws Exception {


        Mockito.when(client.getMetadataById(eq(index), eq(Factory.ID))).thenThrow(new MahutaException());

        // #################################################
        underTest.existsById(Factory.ID);
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void saveIterable() throws Exception {
        // #################################################
        underTest.saveAll(Factory.getEntities(5));
        // #################################################
    }

    @Test
    public void delete() throws Exception {
    	String id = "123";
        
        // #################################################
        underTest.deleteById(id);
        // #################################################
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteIterable() throws Exception {
        // #################################################
        underTest.deleteAll(Factory.getEntities(5));
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
        String hash = "h";
        int total = 10;
        Pageable pagination = PageRequest.of(0, 5);
        Page<Entity> page = Factory.getEntities(total, pagination);

        List<MetadataAndPayload> contentList = page.getContent().stream().map(e -> {
            try {
                Metadata m = new Metadata(index, Factory.ID, hash, null, null);
                return MetadataAndPayload.builder().metadata(m).payload(MAPPER.writeValueAsBytes(e)).build();
            } catch (JsonProcessingException e1) {
                return null;
            }
        }).collect(Collectors.toList());
        Page<MetadataAndPayload> content = new PageImpl<>(contentList, pagination, total);
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
        Pageable pagination = PageRequest.of(0, 5);

        Mockito.when(client.searchAndFetch(eq(index), any(Query.class), eq(pagination))).thenThrow(new MahutaException("error", new Exception()));

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