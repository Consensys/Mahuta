package net.consensys.tools.ipfs.ipfsstore.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerRequest;
import net.consensys.tools.ipfs.ipfsstore.dto.IndexerResponse;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
import net.consensys.tools.ipfs.ipfsstore.dto.query.Query;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;
import net.consensys.tools.ipfs.ipfsstore.exception.NotFoundException;
import net.consensys.tools.ipfs.ipfsstore.exception.ServiceException;
import net.consensys.tools.ipfs.ipfsstore.service.StoreService;
import net.consensys.tools.ipfs.ipfsstore.service.impl.StoreServiceImpl;
import net.consensys.tools.ipfs.ipfsstore.test.dao.ElasticSearchDAOTest;
import net.consensys.tools.ipfs.ipfsstore.test.utils.TestUtils;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class StoreServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreServiceTest.class);

    @MockBean
    private StorageDao storageDao;
    @MockBean
    private IndexDao indexDao;
    
    private StoreService underTest;

    
    @Before
    public void setup() {
        underTest = new StoreServiceImpl(indexDao, storageDao);
    }
    
    @Test
    public void storeFileSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        
        // Mock
        Mockito.when(storageDao.createContent(any(byte[].class))).thenReturn(hash);


        // #################################################
        String hashReturned= underTest.storeFile(pdf);
        // #################################################
         
        ArgumentCaptor<byte[]> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(storageDao, Mockito.times(1)).createContent(argumentCaptorQueryBuilder.capture()); 
        byte[] bytesCaptured = argumentCaptorQueryBuilder.<byte[]> getValue();

        assertEquals(pdf, bytesCaptured);
        assertEquals(hash, hashReturned);
        
    }
    
    @Test(expected=ServiceException.class)
    public void storeFileExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        
        // Mock
        Mockito.when(storageDao.createContent(any(byte[].class))).thenThrow(new DaoException(""));

        // #################################################
        underTest.storeFile(pdf);
        // #################################################
        
    }
    
    @Test
    public void indexFileSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(hash);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(indexDao.index(eq(index), eq(id), eq(hash), eq(contentType), anyList())).thenReturn(id);


        // #################################################
        IndexerResponse response = underTest.indexFile(request);
        // #################################################
         

        assertEquals(id, response.getDocumentId());
        assertEquals(hash, response.getHash());
        assertEquals(index, response.getIndexName());
        
        Mockito.verify(indexDao, Mockito.times(1)).index(eq(index), eq(id), eq(hash), eq(contentType), anyList()); 
        
    }
    
    @Test(expected=ServiceException.class)
    public void indexFileExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(hash);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(indexDao.index(eq(index), eq(id), eq(hash), eq(contentType), anyList())).thenThrow(new DaoException(""));


        // #################################################
        underTest.indexFile(request);
        // #################################################

        
    }
    
    @Test(expected=ServiceException.class)
    public void indexFileInvalidInputTest() throws Exception {

        String hash = null;
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(null);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(indexDao.index(eq(index), eq(id), eq(null), eq(contentType), anyList())).thenThrow(new DaoException(""));


        // #################################################
        underTest.indexFile(request);
        // #################################################
        

        
    }
    
    @Test
    public void storeAndIndexFileSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(hash);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(storageDao.createContent(any(byte[].class))).thenReturn(hash);
        Mockito.when(indexDao.index(eq(index), eq(id), eq(hash), eq(contentType), anyList())).thenReturn(id);


        // #################################################
        IndexerResponse response = underTest.storeAndIndexFile(pdf, request);
        // #################################################
         

        assertEquals(id, response.getDocumentId());
        assertEquals(hash, response.getHash());
        assertEquals(index, response.getIndexName());
        
        ArgumentCaptor<byte[]> argumentCaptorQueryBuilder = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(storageDao, Mockito.times(1)).createContent(argumentCaptorQueryBuilder.capture()); 
        byte[] bytesCaptured = argumentCaptorQueryBuilder.<byte[]> getValue();

        assertEquals(pdf, bytesCaptured);
        
        Mockito.verify(indexDao, Mockito.times(1)).index(eq(index), eq(id), eq(hash), eq(contentType), anyList()); 
        
    }
    
    @Test(expected=ServiceException.class)
    public void storeAndIndexFileUnexpectedExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(hash);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(storageDao.createContent(any(byte[].class))).thenReturn(hash);
        Mockito.when(indexDao.index(eq(index), eq(id), eq(hash), eq(contentType), anyList())).thenThrow(new DaoException(""));


        // #################################################
        underTest.storeAndIndexFile(pdf, request);
        // #################################################
    }
    
    @Test(expected=ServiceException.class)
    public void storeAndIndexFileInvalidInputdExceptionTest() throws Exception {

        String hash = "";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        IndexerRequest request = new IndexerRequest();
        request.setContentType(contentType);
        request.setDocumentId(id);
        request.setHash(hash);
        request.setIndexName(index);
        request.setIndexFields(ElasticSearchDAOTest.getIndexFields(attribute, value));
        
        // Mock
        Mockito.when(storageDao.createContent(any(byte[].class))).thenReturn(hash);
        Mockito.when(indexDao.index(eq(index), eq(id), eq(hash), eq(contentType), anyList())).thenThrow(new DaoException(""));


        // #################################################
        underTest.storeAndIndexFile(pdf, request);
        
        // #################################################
    }
    
    
    @Test
    public void getFileSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String path = "pdf-sample.pdf";
        byte[] pdf = TestUtils.getFile(path);
        
        // Mock
        Mockito.when(storageDao.getContent(eq(hash))).thenReturn(pdf);


        // #################################################
        byte[] fileReturned= underTest.getFileByHash(hash);
        // #################################################
         
        assertEquals(pdf, fileReturned);
        
        Mockito.verify(storageDao, Mockito.times(1)).getContent(eq(hash)); 
        
    }
    
    
    @Test(expected=ServiceException.class)
    public void getFileUnexpectedExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        
        // Mock
        Mockito.when(storageDao.getContent(eq(hash))).thenThrow(new DaoException(""));


        // #################################################
        underTest.getFileByHash(hash);
        // #################################################

        
    }
    
    
    @Test
    public void getFileMetadataSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        Mockito.when(indexDao.searchById(eq(index), eq(id))).thenReturn(new Metadata(index, id, hash, contentType, ElasticSearchDAOTest.getIndexFields(attribute, value)));

        // #################################################
        Metadata metadataReturned = underTest.getFileMetadataById(index, id);
        // #################################################
         
        assertEquals(metadataReturned.getContentType(), contentType);
        assertEquals(metadataReturned.getHash(), hash);
        assertEquals(metadataReturned.getIndexName(), index);
        assertEquals(metadataReturned.getDocumentId(), id);
        assertEquals(metadataReturned.getIndexFieldValue(attribute), value);
        
        Mockito.verify(indexDao, Mockito.times(1)).searchById(eq(index), eq(id)); 
        
    }
    
    
    @Test(expected=NotFoundException.class)
    public void getFileMetadataNotFoundExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        Mockito.when(indexDao.searchById(eq(index), eq(id))).thenThrow(new NotFoundException(""));

        // #################################################
        underTest.getFileMetadataById(index, id);
        // #################################################

        
    }
    
    
    @Test(expected=ServiceException.class)
    public void getFileMetadataUnexpectedExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        Mockito.when(indexDao.searchById(eq(index), eq(id))).thenThrow(new DaoException(""));

        // #################################################
        underTest.getFileMetadataById(index, id);
        // #################################################

        
    }
    
    
    @Test
    public void getFileMetadataByHashSuccessTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        List<Metadata> list = new ArrayList<>();
        list.add(new Metadata(index, id, hash, contentType, ElasticSearchDAOTest.getIndexFields(attribute, value)));
        Mockito.when(indexDao.search(any(Pageable.class), eq(index), any(Query.class))).thenReturn(list);
        Mockito.when(indexDao.count(eq(index), any(Query.class))).thenReturn(Long.valueOf(1));

        // #################################################
        Metadata metadataReturned = underTest.getFileMetadataByHash(index, hash);
        // #################################################
         
        assertEquals(metadataReturned.getContentType(), contentType);
        assertEquals(metadataReturned.getHash(), hash);
        assertEquals(metadataReturned.getIndexName(), index);
        assertEquals(metadataReturned.getDocumentId(), id);
        assertEquals(metadataReturned.getIndexFieldValue(attribute), value);
        
        Mockito.verify(indexDao, Mockito.times(1)).search(any(Pageable.class), eq(index), any(Query.class)); 
        Mockito.verify(indexDao, Mockito.times(1)).count(eq(index), any(Query.class)); 
        
    }
    
    @Test(expected=NotFoundException.class)
    public void getFileMetadataByHashNotFoundTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        List<Metadata> list = new ArrayList<>();
        Mockito.when(indexDao.search(any(Pageable.class), eq(index), any(Query.class))).thenReturn(list);
        Mockito.when(indexDao.count(eq(index), any(Query.class))).thenReturn(Long.valueOf(0));

        // #################################################
        underTest.getFileMetadataByHash(index, hash);
        // ################################################# 
        
    }

    @Test(expected=ServiceException.class)
    public void getFileMetadataByHashUnexpectedExceptionTest() throws Exception {

        String hash = "QmNN4RaVXNMVaEPLrmS7SUQpPZEQ2eJ6s5WxLw9w4GTm34";
        String contentType = "application/json";
        String index = "documents";
        String id = "hello_doc";
        String attribute = "author";
        String value = "Gregoire Jeanmart";
        
        // Mock
        List<Metadata> list = new ArrayList<>();
        list.add(new Metadata(index, id, hash, contentType, ElasticSearchDAOTest.getIndexFields(attribute, value)));
        Mockito.when(indexDao.search(any(Pageable.class), eq(index), any(Query.class))).thenThrow(new DaoException(""));
        Mockito.when(indexDao.count(eq(index), any(Query.class))).thenReturn(Long.valueOf(1));

        // #################################################
        underTest.getFileMetadataByHash(index, hash);
        // #################################################
        
    }
    
    @Test
    public void createTest() throws Exception {

        String index = "AAAAAA";
        

        // #################################################
        underTest.createIndex(index);
        // #################################################
         
        ArgumentCaptor<String> argumentCaptorIndexName = ArgumentCaptor.forClass(String.class);
        Mockito.verify(indexDao, Mockito.times(1)).createIndex(argumentCaptorIndexName.capture()); 
        String indexCaptured = argumentCaptorIndexName.<String> getValue();

        assertEquals(index, indexCaptured);
        
    }
    
}
