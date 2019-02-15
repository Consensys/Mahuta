package net.consensys.mahuta.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import net.consensys.mahuta.core.exception.TechnicalException;

public class FileUtils {

    private FileUtils() { }

    public static InputStream readFileInputStream(String path) {
        
        if(ValidatorUtils.isEmpty(path)) {
            return null;
        }

        try {
            ClassLoader classLoader = FileUtils.class.getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());

            return new FileInputStream(file);

        } catch (FileNotFoundException e) {
            throw new TechnicalException("File cannot be found...", e);
        }
    }

    public static byte[] readFile(String path) {

        try(FileInputStream fileInputStream = (FileInputStream) readFileInputStream(path);) {
            return IOUtils.toByteArray(fileInputStream);

        } catch (IOException e) {
            throw new TechnicalException("File cannot be found...", e);
        }
    }
}
