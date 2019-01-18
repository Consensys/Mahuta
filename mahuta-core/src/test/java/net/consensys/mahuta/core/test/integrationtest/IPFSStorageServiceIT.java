package net.consensys.mahuta.core.test.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.IntegrationTestUtils;
import net.consensys.mahuta.core.utils.FileUtils;

@Slf4j
public class IPFSStorageServiceIT extends IntegrationTestUtils {

    @Test
    public void connection() throws Exception {
        //////////////////////////////
        IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort()).configureThreadPool(20)
                .configureTimeout(5000);
        //////////////////////////////
    }

    @Test
    public void connectionMultiAddress() throws Exception {
        String multiaddress = "/ip4/127.0.0.1/tcp/" + ipfsContainer1.getFirstMappedPort();

        //////////////////////////////
        IPFSService.connect(multiaddress);
        //////////////////////////////
    }

    @Test(expected = ConnectionException.class)
    public void connectionException() throws Exception {
        //////////////////////////////
        IPFSService.connect("fdsfdsfdsf", ipfsContainer1.getFirstMappedPort());
        //////////////////////////////
    }

    @Test
    public void write() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(FILE_PATH));
        //////////////////////////////

        assertEquals(FILE_HASH, hash);
    }

    @Test
    public void write2() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(FILE_PATH2));
        //////////////////////////////

        assertEquals(FILE_HASH2, hash);
    }

    @Test
    public void getPinnedFiles() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(FILE_PATH2));
        List<String> hashes = service.getPinned();
        log.debug("hashes: {}", hashes);
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(hash)));
    }

    @Test
    public void pinFile() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());

        //////////////////////////////
        service.pin(TEXT_SAMPLE_HASH);
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(TEXT_SAMPLE_HASH)));
    }

    @Test
    public void unpinFile() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());

        //////////////////////////////
        service.unpin(TEXT_SAMPLE_HASH);
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertFalse(hashes.stream().anyMatch(h -> h.equals(TEXT_SAMPLE_HASH)));
    }

    @Test
    public void read() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort());
        service.write(FileUtils.readFileInputString(FILE_PATH));

        //////////////////////////////
        ByteArrayOutputStream content = (ByteArrayOutputStream) service.read(FILE_HASH);
        //////////////////////////////

        assertEquals(FileUtils.readFile(FILE_PATH).length, content.size());
    }

    @Test(expected = TimeoutException.class)
    public void readTimeoutException() throws Exception {
        StorageService service = IPFSService.connect(ipfsContainer1.getContainerIpAddress(), ipfsContainer1.getFirstMappedPort())
                .configureTimeout(1);
        service.write(FileUtils.readFileInputString(FILE_PATH));

        //////////////////////////////
        service.read(FILE_HASH, new ByteArrayOutputStream());
        //////////////////////////////
    }

}
