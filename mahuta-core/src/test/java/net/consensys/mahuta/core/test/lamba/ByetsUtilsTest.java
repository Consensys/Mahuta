package net.consensys.mahuta.core.test.lamba;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import net.consensys.mahuta.core.test.utils.FileTestUtils;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;
import net.consensys.mahuta.core.test.utils.TestUtils;
import net.consensys.mahuta.core.utils.BytesUtils;

public class ByetsUtilsTest extends TestUtils {

    @Test
    public void convertISToByteArray() {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        byte[] result = BytesUtils.convertToByteArray(file.getIs());
        assertEquals(file.getBytearray().length, result.length);
    }
    @Test
    public void convertBAToOutputStream() {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        ByteArrayOutputStream os = (ByteArrayOutputStream) BytesUtils.convertToOutputStream(file.getBytearray());
        assertEquals(file.getBytearray().length, os.toByteArray().length);
    }
    
}
