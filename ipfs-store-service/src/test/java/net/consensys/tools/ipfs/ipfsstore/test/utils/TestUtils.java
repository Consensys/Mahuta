package net.consensys.tools.ipfs.ipfsstore.test.utils;

import java.io.File;
import java.io.FileInputStream;

public abstract class TestUtils {
    
    public static byte[] getFile(String path) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());

        FileInputStream fileInputStream = new FileInputStream(file);
        
        long byteLength = file.length(); //bytecount of the file-content

        byte[] filecontent = new byte[(int) byteLength];
        fileInputStream.read(filecontent, 0, (int) byteLength);
        
        return filecontent;
      }
}
