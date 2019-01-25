package net.consensys.mahuta.core.test.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.test.utils.ContainerUtils;
import net.consensys.mahuta.core.test.utils.ContainerUtils.ContainerType;
import net.consensys.mahuta.core.test.utils.FileTestUtils;
import net.consensys.mahuta.core.test.utils.FileTestUtils.FileInfo;
import net.consensys.mahuta.core.test.utils.TestUtils;
import net.consensys.mahuta.core.utils.lamba.Throwing;

@Slf4j
public class IPFSStorageServiceTest extends TestUtils {

    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ContainerUtils.startContainer("ipfs1", ContainerType.IPFS);
        ContainerUtils.startContainer("ipfs2", ContainerType.IPFS);
        
        IPFS ipfs = new IPFS(ContainerUtils.getHost("ipfs2"), ContainerUtils.getPort("ipfs2"));
        FileTestUtils.files.forEach(Throwing.rethrow((path, file) -> {
            ipfs.add(new NamedStreamable.ByteArrayWrapper(file.getBytearray()));
        }));
        
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

    @Test(expected = ConnectionException.class)
    public void connectionException2() throws Exception {
        //////////////////////////////
        IPFSService.connect();
        //////////////////////////////
    }

    @Test
    public void write() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(file.getIs());
        //////////////////////////////

        assertEquals(file.getCid(), hash);
    }

    @Test
    public void write2() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(file.getBytearray());
        //////////////////////////////

        assertEquals(file.getCid(), hash);
    }

    @Test
    public void getPinnedFiles() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        String hash = service.write(file.getIs());
        List<String> hashes = service.getPinned();
        log.debug("hashes: {}", hashes);
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(hash)));
    }

    @Test
    public void pinFile() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        service.pin(file.getCid());
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertTrue(hashes.stream().anyMatch(h -> h.equals(file.getCid())));
    }

    @Test
    public void unpinFile() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));

        //////////////////////////////
        service.unpin(file.getCid());
        List<String> hashes = service.getPinned();
        //////////////////////////////
        
        assertFalse(hashes.stream().anyMatch(h -> h.equals(file.getCid())));
    }

    @Test
    public void read() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"));
        service.write(file.getIs());

        //////////////////////////////
        ByteArrayOutputStream content = (ByteArrayOutputStream) service.read(file.getCid());
        //////////////////////////////

        assertEquals(file.getBytearray().length, content.size());
    }

    @Test(expected = TimeoutException.class)
    @Ignore("Not a stable test")
    public void readTimeoutException() throws Exception {
        FileInfo file = mockNeat.fromValues(FileTestUtils.files).get();
        StorageService service = IPFSService.connect(ContainerUtils.getHost("ipfs1"), ContainerUtils.getPort("ipfs1"))
                .configureTimeout(1);
        service.write(file.getIs());

        //////////////////////////////
        service.read(file.getCid(), new ByteArrayOutputStream());
        //////////////////////////////
    }

}
