package net.consensys.mahuta.core.test.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import lombok.Getter;
import lombok.ToString;
import net.consensys.mahuta.core.utils.FileUtils;

public class FileTestUtils extends TestUtils {
    
    public static final Map<String, FileInfo> files = ImmutableMap.of(
            "pdf-sample.pdf", new FileInfo("pdf-sample.pdf", "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o", "application/pdf"),
            "pdf-sample2.pdf", new FileInfo("pdf-sample2.pdf", "QmaNxbQNrJdLzzd8CKRutBjMZ6GXRjvuPepLuNSsfdeJRJ", "application/pdf"),
            "text-sample.txt", new FileInfo("text-sample.txt", "QmUXTtySmd7LD4p6RG6rZW6RuUuPZXTtNMmRQ6DSQo3aMw", "text/plain")
    );
    
    public static FileInfo newRandomPlainText(IPFS ipfs) {
        
        try {
            String content = mockNeat.strings().size(500).get();
            return new FileInfo(ipfs, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
            
    @ToString
    public static class FileInfo {
        private @Getter String path;
        private @Getter String cid;
        private @Getter String type;
        private @Getter byte[] bytearray;
        
        public InputStream getIs() {
            return new ByteArrayInputStream(bytearray);
        }
        
        public FileInfo(String path, String cid, String type) {
            this.path = path;
            this.cid = cid;
            this.type = type;
            this.bytearray = FileUtils.readFile(path);
        }
        public FileInfo(IPFS ipfs, String type, byte[] bytearray) throws IOException {
            this.path = null;
            this.cid = ipfs.add(new NamedStreamable.ByteArrayWrapper(bytearray)).get(0).hash.toString();
            this.type = type;
            this.bytearray = bytearray;
        }
        public FileInfo(IPFS ipfs, String content) throws IOException {
            this(ipfs, "text/plain", content.getBytes());
        }
    }
}
