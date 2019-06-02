package net.consensys.mahuta.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import com.google.common.io.ByteSource;

import net.consensys.mahuta.core.exception.TechnicalException;

public class BytesUtils {

    private BytesUtils() { }

    public static InputStream readFileInputStream(String path) {
        
        if(ValidatorUtils.isEmpty(path)) {
            return null;
        }

        try {
            ClassLoader classLoader = BytesUtils.class.getClassLoader();
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
    
    public static byte[] convertToByteArray(InputStream is) {
        try {
            return IOUtils.toByteArray(is);
        }  catch (IOException e) {
            throw new TechnicalException("Error while converting InputStream to byte array", e);
        }
    }
    
    public static byte[] convertToByteArray(OutputStream os) {
        try {    
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.writeTo(os);
            bos.close();
            return bos.toByteArray();
        }  catch (IOException e) {
            throw new TechnicalException("Error while converting OutputStream to byte array", e);
        }
    }
    
    public static InputStream convertToInputStream(byte[] bytesarray) {
        try {
            return ByteSource.wrap(bytesarray).openStream();
        }  catch (IOException e) {
            throw new TechnicalException("Error while converting bytes array to InputStream", e);
        }
    }
    
    public static OutputStream convertToOutputStream(byte[] bytesarray) {
        try {
            OutputStream os = new ByteArrayOutputStream();
            os.write(bytesarray);
            return os;
        }  catch (IOException e) {
            throw new TechnicalException("Error while converting bytes array to OutputStream", e);
        }
    }
}
