package net.consensys.tools.ipfs.ipfsstore.client.java.test.wrapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;


@RunWith(SpringRunner.class)
public class IPFSStoreTest {
    private static final Logger LOG = LoggerFactory.getLogger(IPFSStoreTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENDPOINT = "http://localhost:8040";
    private static final String INDEX_NAME = "documents";

    private MockRestServiceServer mockServer;

    private IPFSStore undertest;



    /* ********************************************************************
     * SETUP
     * ******************************************************************** */

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.undertest = new IPFSStore(ENDPOINT);

        this.mockServer = MockRestServiceServer.createServer(this.undertest.getWrapper().getClient());

        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }



    /* ********************************************************************
     * STORE
     * ******************************************************************** */

    @Test
    public void storeTestFilePath() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        // MOCK
        String responseStore =
                "{\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/raw/store"))
                .andExpect(header("content-type", containsString("multipart/form-data")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));

        // ###########################
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());
        String hashReturned = this.undertest.store(file.getAbsolutePath());
        // ###########################

        LOG.info("hashReturned=" + hashReturned);

        mockServer.verify();
        assertEquals(hash, hashReturned);
    }

    @Test
    public void storeTestInputStream() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        // MOCK
        String responseStore =
                "{\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/raw/store"))
                .andExpect(header("content-type", containsString("multipart/form-data")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));

        // ###########################
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());
        String hashReturned = this.undertest.store(new FileInputStream(file));
        // ###########################

        LOG.info("hashReturned=" + hashReturned);

        mockServer.verify();
        assertEquals(hash, hashReturned);
    }

    @Test(expected = IPFSStoreException.class)
    public void storeTestWrongfile() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        // MOCK
        String responseStore =
                "{\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/store"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));

        // ###########################
        this.undertest.store("donotexist.pdf");
        // ###########################
    }


    /* ********************************************************************
     * INDEX
     * ******************************************************************** */

    @Test
    public void indexTest() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"index\": \"" + INDEX_NAME + "\",\n" +
                        "    \"id\": \"" + id + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/raw/index"))
                .andExpect(header("content-type", containsString("application/json")))
                .andExpect(jsonPath("index", containsString(INDEX_NAME)))
                .andExpect(jsonPath("hash", containsString(hash)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        String idReturned = this.undertest.index(INDEX_NAME, hash);
        // ###########################


        LOG.info("idReturned=" + idReturned);

        assertEquals(id, idReturned);
    }

    @Test
    public void indexMapTest() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";
        String contentType = "application/pdf";
        Map<String, Object> indexFields = new HashMap<>();
        String authorKey = "author";
        String authorVal = "greg";
        indexFields.put(authorKey, authorVal);

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"index\": \"" + INDEX_NAME + "\",\n" +
                        "    \"id\": \"" + id + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/raw/index"))
                .andExpect(header("content-type", containsString("application/json")))
                .andExpect(jsonPath("index", containsString(INDEX_NAME)))
                .andExpect(jsonPath("hash", containsString(hash)))
                .andExpect(jsonPath("id", containsString(id)))
                .andExpect(jsonPath("content_type", containsString(contentType)))
                .andExpect(jsonPath("index_fields[0].name", containsString(authorKey)))
                .andExpect(jsonPath("index_fields[0].value", containsString(authorVal)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        String idReturned = this.undertest.index(INDEX_NAME, hash, id, contentType, indexFields);
        // ###########################


        LOG.info("idReturned=" + idReturned);

        assertEquals(id, idReturned);
    }



    /* ********************************************************************
     * STORE & INDEX
     * ******************************************************************** */

    @Test
    public void storeAndIndexTestFilePath() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"index\": \"" + INDEX_NAME + "\",\n" +
                        "    \"id\": \"" + id + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(containsString(ENDPOINT + "/ipfs-store/raw/store_index")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));


        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());

        // ###########################
        String idReturned = this.undertest.index(new FileInputStream(file), INDEX_NAME);
        // ###########################


        LOG.info("idReturned=" + idReturned);

        assertEquals(id, idReturned);
    }

    @Test
    public void storeAndIndexTestFilePath2() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";
        String contentType = "application/pdf";
        Map<String, Object> indexFields = new HashMap<>();
        String authorKey = "author";
        String authorVal = "greg";
        indexFields.put(authorKey, authorVal);

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"index\": \"" + INDEX_NAME + "\",\n" +
                        "    \"id\": \"" + id + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(containsString(ENDPOINT + "/ipfs-store/raw/store_index")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));


        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());

        // ###########################
        String idReturned = this.undertest.index(new FileInputStream(file), INDEX_NAME, id, contentType, indexFields);
        // ###########################


        LOG.info("idReturned=" + idReturned);

        assertEquals(id, idReturned);
    }

    @Test(expected = IPFSStoreException.class)
    public void storeAndIndexExceptionTestFilePath() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String id = "ABC";
        String contentType = "application/pdf";
        Map<String, Object> indexFields = new HashMap<>();
        String authorKey = "author";
        String authorVal = "greg";
        indexFields.put(authorKey, authorVal);

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"index\": \"" + INDEX_NAME + "\",\n" +
                        "    \"id\": \"" + id + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        mockServer.expect(requestTo(containsString(ENDPOINT + "/ipfs-store/raw/store_index")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());


        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());

        // ###########################
        this.undertest.index(new FileInputStream(file), INDEX_NAME, id, contentType, indexFields);
        // ###########################

    }

    /* ********************************************************************
     * GET
     * ******************************************************************** */

    @Test
    public void getTest() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());
        FileInputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);

        // MOCK
        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/fetch/" + hash + "?index=" + INDEX_NAME))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(bytes, MediaType.APPLICATION_PDF));

        // ###########################
        byte[] contentReturned = this.undertest.get(INDEX_NAME, hash);
        // ###########################

        assertEquals(bytes.length, contentReturned.length);
    }

    @Test(expected = IPFSStoreException.class)
    public void getExceptionTest() throws Exception {

        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("pdf-sample.pdf")).getFile());
        FileInputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);

        // MOCK
        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/fetch/" + hash + "?index=" + INDEX_NAME))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // ###########################
        this.undertest.get(INDEX_NAME, hash);
        // ###########################
    }



    /* ********************************************************************
     * GET METADATA BY ID
     * ******************************************************************** */

    @Test
    public void getMetadataByIdTest() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseStore =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 0\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=documents&page=0&size=1"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));

        // ###########################
        Metadata metadata = this.undertest.getMetadataById(INDEX_NAME, id);
        // ###########################

        assertEquals(INDEX_NAME, metadata.getIndex());
        assertEquals(id, metadata.getDocumentId());
        assertEquals(hash, metadata.getHash());
        assertEquals(contentType, metadata.getContentType());
        assertEquals(author, metadata.getIndexFieldValue("author"));
    }


    @Test
    public void getMetadataByIdNotFoundTest() throws Exception {

        String id = "ABC";
        // MOCK
        String responseStore =
                "{\n" +
                        "    \"content\": [],\n" +
                        "    \"numberOfElements\": 0,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 0,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 0\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=documents&page=0&size=1"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseStore, MediaType.APPLICATION_JSON));

        // ###########################
        Metadata metadata = this.undertest.getMetadataById(INDEX_NAME, id);
        // ###########################

        assertNull(metadata);
    }

    @Test(expected = IPFSStoreException.class)
    public void getMetadataByIdTTest() throws Exception {

        String id = "ABC";

        // MOCK
        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=documents&page=0&size=1"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // ###########################
        this.undertest.getMetadataById(INDEX_NAME, id);
        // ###########################
    }




    /* ********************************************************************
     * SEARCH
     * ******************************************************************** */

    @Test
    public void searchAllTest() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 1\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=" + INDEX_NAME))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        Page<Metadata> result = this.undertest.search(INDEX_NAME);
        // ###########################


        LOG.info(result.toString());

        assertEquals(1, result.getTotalElements());
        assertEquals(INDEX_NAME, result.getContent().get(0).getIndex());
        assertEquals(id, result.getContent().get(0).getDocumentId());
        assertEquals(hash, result.getContent().get(0).getHash());
        assertEquals(contentType, result.getContent().get(0).getContentType());
        assertEquals(author, result.getContent().get(0).getIndexFieldValue("author"));
    }

    @Test
    public void searchAllWithPaginationTest() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 0\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index="+INDEX_NAME+"&page=0&size=2"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        Page<Metadata> result = this.undertest.search(INDEX_NAME, null, new PageRequest(0, 2));
        // ###########################


        LOG.info(result.toString());

        assertEquals(1, result.getTotalElements());
        assertEquals(INDEX_NAME, result.getContent().get(0).getIndex());
        assertEquals(id, result.getContent().get(0).getDocumentId());
        assertEquals(hash, result.getContent().get(0).getHash());
        assertEquals(contentType, result.getContent().get(0).getContentType());
        assertEquals(author, result.getContent().get(0).getIndexFieldValue("author"));
    }


    @Test
    public void searchAllWithPaginationTest2() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 1\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=" + INDEX_NAME + "&page=1&size=2"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        Page<Metadata> result = this.undertest.search(INDEX_NAME, null, 1, 2);
        // ###########################


        LOG.info(result.toString());

        assertEquals(result.getTotalElements(), 1);
        assertEquals(result.getContent().get(0).getIndex(), INDEX_NAME);
        assertEquals(result.getContent().get(0).getDocumentId(), id);
        assertEquals(result.getContent().get(0).getHash(), hash);
        assertEquals(result.getContent().get(0).getContentType(), contentType);
        assertEquals(result.getContent().get(0).getIndexFieldValue("author"), author);
    }

    @Test
    public void searchAllWithPaginationAndSortingTest() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 0\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index=" + INDEX_NAME + "&page=0&size=2&sort=id&dir=DESC"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        Page<Metadata> result = this.undertest.search(INDEX_NAME, null, new PageRequest(0, 2, Sort.Direction.DESC, "id"));
        // ###########################


        LOG.info(result.toString());

        assertEquals(1, result.getTotalElements());
        assertEquals(INDEX_NAME, result.getContent().get(0).getIndex());
        assertEquals(id, result.getContent().get(0).getDocumentId());
        assertEquals(hash, result.getContent().get(0).getHash());
        assertEquals(contentType, result.getContent().get(0).getContentType());
        assertEquals(author, result.getContent().get(0).getIndexFieldValue("author"));
    }


    @Test
    public void searchAllWithPaginationAndSortingTest2() throws Exception {

        String id = "ABC";
        String contentType = "application/pdf";
        String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
        String author = "Greg";

        // MOCK
        String responseIndex =
                "{\n" +
                        "    \"content\": [\n" +
                        "        {\n" +
                        "            \"index\": \"" + INDEX_NAME + "\",\n" +
                        "            \"id\": \"" + id + "\",\n" +
                        "            \"hash\": \"" + hash + "\",\n" +
                        "            \"content_type\": \"" + contentType + "\",\n" +
                        "            \"index_fields\": [\n" +
                        "                {\n" +
                        "                    \"name\": \"__content_type\",\n" +
                        "                    \"value\": \"" + contentType + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"date_created\",\n" +
                        "                    \"value\": 1518700549\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"author\",\n" +
                        "                    \"value\": \"" + author + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"__hash\",\n" +
                        "                    \"value\": \"" + hash + "\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"votes\",\n" +
                        "                    \"value\": 4\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"name\": \"title\",\n" +
                        "                    \"value\": \"Hello Doc\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"numberOfElements\": 1,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 1,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 2,\n" +
                        "    \"number\": 1\n" +
                        "}";

        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/query/search?index="+INDEX_NAME+"&page=1&size=2&sort=id&dir=DESC"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseIndex, MediaType.APPLICATION_JSON));

        // ###########################
        Page<Metadata> result = this.undertest.search(INDEX_NAME, null, 1, 2, "id", Sort.Direction.DESC);
        // ###########################


        LOG.info(result.toString());

        assertEquals(1, result.getTotalElements());
        assertEquals(INDEX_NAME, result.getContent().get(0).getIndex());
        assertEquals(id, result.getContent().get(0).getDocumentId());
        assertEquals(hash, result.getContent().get(0).getHash());
        assertEquals(contentType, result.getContent().get(0).getContentType());
        assertEquals(author, result.getContent().get(0).getIndexFieldValue("author"));
    }


    @Test
    public void createIndex() throws Exception {

        // MOCK
        mockServer.expect(requestTo(ENDPOINT + "/ipfs-store/config/index/" + INDEX_NAME))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        // ###########################
        this.undertest.createIndex(INDEX_NAME);
        // ###########################


    }

}
