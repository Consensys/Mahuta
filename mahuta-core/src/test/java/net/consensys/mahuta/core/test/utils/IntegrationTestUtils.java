package net.consensys.mahuta.core.test.utils;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import net.consensys.mahuta.core.utils.FileUtils;

public abstract class IntegrationTestUtils {
    
    protected static final String FILE_PATH = "pdf-sample.pdf";
    protected static final String FILE_PATH2 = "pdf-sample2.pdf";
    protected static final String FILE_HASH = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
    protected static final String FILE_HASH2 = "QmaNxbQNrJdLzzd8CKRutBjMZ6GXRjvuPepLuNSsfdeJRJ";
    protected static final String FILE_TYPE = "application/pdf";
    protected static final String CID = "QmTeW79w7QQ6Npa3b1d5tANreCDxF2iDaAPsDvW6KtLmfB";
    
    protected static final String TEXT_SAMPLE = "text-sample.txt";
    protected static final String TEXT_SAMPLE_HASH = "QmUXTtySmd7LD4p6RG6rZW6RuUuPZXTtNMmRQ6DSQo3aMw";

    protected static GenericContainer ipfsContainer1;
    protected static GenericContainer ipfsContainer2;

    @BeforeClass
    public static void startContainers() throws IOException, InterruptedException {
        ipfsContainer1 = new GenericContainer("jbenet/go-ipfs").withExposedPorts(5001, 4001, 8080);
        ipfsContainer1.start();
        ipfsContainer2 = new GenericContainer("jbenet/go-ipfs").withExposedPorts(5001, 4001, 8080);
        ipfsContainer2.start();
        
        
        IPFS ipfs = new IPFS(ipfsContainer2.getContainerIpAddress(), ipfsContainer2.getFirstMappedPort());
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(FileUtils.readFile(TEXT_SAMPLE));
        ipfs.add(file);
    }
    
    @AfterClass
    public static void stopContainers() {
        ipfsContainer1.stop();
        ipfsContainer2.stop();
    }
}
