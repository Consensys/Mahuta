package net.consensys.mahuta.core.indexer.elasticsearch.test.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Test;

import net.andreinc.mockneat.types.enums.StringType;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.Metadata;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.indexing.IndexingRequest;
import net.consensys.mahuta.core.indexer.elasticsearch.test.utils.IntegrationTestUtils;
import net.consensys.mahuta.core.utils.FileUtils;

public class MahutaIT extends IntegrationTestUtils {
    

    @Test
    public void build() throws Exception {
        ////////////////////////
        get();
        ///////////////////////
    }

    @Test
    public void indexFile() throws Exception {
        String indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        
        Mahuta mahuta = get(indexName, FileUtils.readFileInputString("index_mapping.json"));
        
        IndexingRequest request = generateRandomInputStreamIndexingRequest(indexName, FILE);

        ////////////////////////
        Metadata metadata = mahuta.index(request);
        ///////////////////////
        
        assertTrue(indexName.equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(request.getIndexDocId(), metadata.getIndexDocId());
        assertEquals(FILE_HASH, metadata.getContentId());
        assertEquals(FILE_TYPE, metadata.getContentType());
        assertEquals(request.getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(request.getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(request.getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(request.getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(request.getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }

    @Test
    public void indexString() throws Exception {
        String indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        
        Mahuta mahuta = get(indexName, FileUtils.readFileInputString("index_mapping.json"));
        
        IndexingRequest request = generateRandomStringIndexingRequest(indexName);

        ////////////////////////
        Metadata metadata = mahuta.index(request);
        ///////////////////////

        assertTrue(indexName.equalsIgnoreCase(metadata.getIndexName()));
        assertEquals(request.getIndexDocId(), metadata.getIndexDocId());
        assertNotNull(metadata.getContentId());
        assertEquals(TXT_TYPE, metadata.getContentType());
        assertEquals(request.getIndexFields().get(AUTHOR_FIELD), metadata.getIndexFields().get(AUTHOR_FIELD));
        assertEquals(request.getIndexFields().get(TITLE_FIELD), metadata.getIndexFields().get(TITLE_FIELD));
        assertEquals(request.getIndexFields().get(IS_PUBLISHED_FIELD), metadata.getIndexFields().get(IS_PUBLISHED_FIELD));
        assertEquals(request.getIndexFields().get(DATE_CREATED_FIELD), metadata.getIndexFields().get(DATE_CREATED_FIELD));
        assertEquals(request.getIndexFields().get(VIEWS_FIELD), metadata.getIndexFields().get(VIEWS_FIELD));
    }

    @Test
    public void searchNoQuery() throws Exception {
        Integer noDocs = 50;
        Integer nbPages = 3;
        String indexName = mockNeat.strings().size(20).type(StringType.ALPHA_NUMERIC).get();
        
        Mahuta mahuta = get(indexName, FileUtils.readFileInputString("index_mapping.json"));
        
        ////////////////////////
        IntStream.range(0, noDocs).forEach(i-> {
             mahuta.index(generateRandomStringIndexingRequest(indexName));
        });
        
        Page<Metadata> result = mahuta.search(indexName);
        ///////////////////////
        
        assertEquals(noDocs, result.getTotalElements());
        assertEquals(nbPages, result.getTotalPages());
        
        
    }

}
