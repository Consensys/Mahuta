package net.consensys.mahuta.core.test.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ConstantUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.utils.FileUtils;

@Slf4j
public class IPFSStorageServiceIT {

    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ContainerUtils.startContainer("ipfs1", ContainerType.IPFS);
        ContainerUtils.startContainer("ipfs2", ContainerType.IPFS);
        
        IPFS ipfs = new IPFS(ContainerUtils.getHost("ipfs2"), ContainerUtils.getPort("ipfs2"));
        ipfs.add(new NamedStreamable.ByteArrayWrapper(FileUtils.readFile(ConstantUtils.TEXT_SAMPLE_PATH)));
    }
    
    @AfterClass
    public static void stopContainers() {
        ContainerUtils.stopAll();
    }
    
    @Test
    public void connection() throws Exception {
        //////////////////////////////
        IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1")).configureThreadPool(20)
                .configureTimeout(5000);
        //////////////////////////////
    }

    @Test
    public void connectionMultiAddress() throws Exception {
        String multiaddress = "/ip4/127.0.0.1/tcp/" + ContainerUtils.getPort("ipfs1");

        //////////////////////////////
        IPFSService.connect(multiaddress);
        //////////////////////////////
    }

    @Test(expected = ConnectionException.class)
    public void connectionException() throws Exception {
        //////////////////////////////
        IPFSService.connect("fdsfdsfdsf", ContainerUtils.getPort("ipfs2"));
        //////////////////////////////
    }

    @Test
    public void write() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(ConstantUtils.FILE_PATH));
        //////////////////////////////

        assertEquals(ConstantUtils.FILE_HASH, hash);
    }

    @Test
    public void write2() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(ConstantUtils.FILE_PATH2));
        //////////////////////////////

        assertEquals(ConstantUtils.FILE_HASH2, hash);
    }

    @Test
    public void getPinnedFiles() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(FileUtils.readFileInputString(ConstantUtils.FILE_PATH2));
        List<String> hashes = service.getPinned();
        log.debug("hashes: {}", hashes);
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(hash)));
    }

    @Test
    public void pinFile() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        service.pin(ConstantUtils.TEXT_SAMPLE_HASH);
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(ConstantUtils.TEXT_SAMPLE_HASH)));
    }

    @Test
    public void unpinFile() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        service.unpin(ConstantUtils.TEXT_SAMPLE_HASH);
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertFalse(hashes.stream().anyMatch(h -> h.equals(ConstantUtils.TEXT_SAMPLE_HASH)));
    }

    @Test
    public void read() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));
        service.write(FileUtils.readFileInputString(ConstantUtils.FILE_PATH));

        //////////////////////////////
        ByteArrayOutputStream content = (ByteArrayOutputStream) service.read(ConstantUtils.FILE_HASH);
        //////////////////////////////

        assertEquals(FileUtils.readFile(ConstantUtils.FILE_PATH).length, content.size());
    }

    @Test(expected = TimeoutException.class)
    public void readTimeoutException() throws Exception {
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"))
                .configureTimeout(1);
        service.write(FileUtils.readFileInputString(ConstantUtils.FILE_PATH));

        //////////////////////////////
        service.read(ConstantUtils.FILE_HASH, new ByteArrayOutputStream());
        //////////////////////////////
    }

}
