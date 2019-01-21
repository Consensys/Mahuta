package net.consensys.mahuta.core.test.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import net.consensys.mahuta.core.utils.FileUtils;

public class FileTestUtils {
    
    public static final List< FileInfo> files = Arrays.asList(
            new FileInfo("pdf-sample.pdf", "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o", "application/pdf"),
            new FileInfo("pdf-sample2.pdf", "QmaNxbQNrJdLzzd8CKRutBjMZ6GXRjvuPepLuNSsfdeJRJ", "application/pdf"),
            new FileInfo("text-sample.txt", "QmUXTtySmd7LD4p6RG6rZW6RuUuPZXTtNMmRQ6DSQo3aMw", "text/plain")
    );
            

    static class FileInfo {
        private @Getter String path;
        private @Getter String cid;
        private @Getter String type;
        private @Getter InputStream is;
        
        public FileInfo(String path, String cid, String type) {
            this.path = path;
            this.cid = cid;
            this.type = type;
            this.is = FileUtils.readFileInputString(path);
        }
    }
}
