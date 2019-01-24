package net.consensys.mahuta.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import net.consensys.mahuta.core.exception.TechnicalException;

public class FileUtils {

    private FileUtils() {
    }

    public static InputStream readFileInputStream(String path) {
        
        if(ValidatorUtils.isEmpty(path)) {
            return null;
        }

        try {
            ClassLoader classLoader = FileUtils.class.getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());

            return new FileInputStream(file);

        } catch (FileNotFoundException e) {
            throw new TechnicalException(e);
        }
    }

    public static byte[] readFile(String path) {

        try {
            ClassLoader classLoader = FileUtils.class.getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());

            FileInputStream fileInputStream = (FileInputStream) readFileInputStream(path);

            long byteLength = file.length(); // bytecount of the file-content

            byte[] filecontent = new byte[(int) byteLength];
            fileInputStream.read(filecontent, 0, (int) byteLength);
            fileInputStream.close();

            return filecontent;

        } catch (IOException e) {
            throw new TechnicalException(e);
        }
    }
}
