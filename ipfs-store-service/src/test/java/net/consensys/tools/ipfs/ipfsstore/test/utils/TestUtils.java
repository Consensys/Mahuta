package net.consensys.tools.ipfs.ipfsstore.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public abstract class TestUtils {
    
    public static byte[] getFile(String path) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());

        FileInputStream fileInputStream = new FileInputStream(file);
        
        long byteLength = file.length(); //bytecount of the file-content

        byte[] filecontent = new byte[(int) byteLength];
        fileInputStream.read(filecontent, 0, (int) byteLength);
        
        return filecontent;
      }
    
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
