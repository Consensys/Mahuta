package net.consensys.mahuta.core.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import net.consensys.mahuta.core.domain.common.Metadata;
import net.consensys.mahuta.core.test.utils.TestUtils;

public class MiscTest extends TestUtils {

    @Test
    public void convertISToByteArray() {
        String indexName = mockNeat.strings().get();
        String indexDocId = mockNeat.strings().get();
        String contentId = mockNeat.strings().get();
        String contentType = mockNeat.strings().get();
        byte[] content = new byte[] {1,0};
        boolean pinned = mockNeat.bools().get();
        Map<String, Object> indexFields = ImmutableMap.of("k", mockNeat.strings().get());
        
        Metadata metadata = Metadata.of(indexName, indexDocId, contentId, contentType, content, pinned, indexFields);
        
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(indexDocId, metadata.getIndexDocId());
        assertEquals(contentId, metadata.getContentId());
        assertEquals(contentType, metadata.getContentType());
        assertEquals(content, metadata.getContent());
        assertEquals(pinned, metadata.isPinned());
        assertEquals(indexFields, metadata.getIndexFields());
    }

    
}
