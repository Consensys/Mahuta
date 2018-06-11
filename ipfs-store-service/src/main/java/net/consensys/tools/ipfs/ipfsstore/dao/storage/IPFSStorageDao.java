package net.consensys.tools.ipfs.ipfsstore.dao.storage;

import static net.consensys.tools.ipfs.ipfsstore.Settings.ERROR_NOT_NULL_OR_EMPTY;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.StorageConfiguration;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;
import net.consensys.tools.ipfs.ipfsstore.exception.TimeoutException;

/**
 * IPFS implementation of StorageDao
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Slf4j
public class IPFSStorageDao implements StorageDao {

    private static final String THREAD_POOL_PARAM = "thread_pool";
    private static final int DEFAULT_THREAD_POOL = 10;
    private static final String TIMEOUT_PARAM = "timeout";
    private static final int DEFAULT_TIMEOUT = 10000;

    private final IPFS ipfs;
    private final ExecutorService pool;
    private final Integer timeout;

    public IPFSStorageDao(StorageConfiguration storageConfiguration, IPFS ipfs) {
        this.ipfs = ipfs;
        
        this.pool = Executors.newFixedThreadPool(storageConfiguration.getAdditionalParam(THREAD_POOL_PARAM)
                .map(Integer::valueOf)
                .orElse(DEFAULT_THREAD_POOL));
        
        this.timeout = storageConfiguration.getAdditionalParam(TIMEOUT_PARAM)
                .map(Integer::valueOf)
                .orElse(DEFAULT_TIMEOUT);
    }
    
    @Override
    public Result check() {
      log.debug("check IPFS health ...");
      
      try {
        ipfs.config.show();
        log.debug("IPFS is OK");
        return Result.healthy();
      } catch(Exception e) {
        log.error("IPFS is KO", e);
        return Result.unhealthy(e);
      }
    }

    @Override
    public String createContent(byte[] content) {

        log.debug("Store file in IPFS ...");

        // Validation
        if (content == null) throw new IllegalArgumentException("content " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            NamedStreamable.ByteArrayWrapper requestFile = new NamedStreamable.ByteArrayWrapper(content);
            MerkleNode response = this.ipfs.add(requestFile).get(0);

            String hash = response.hash.toString();

            log.debug("File created in IPFS: hash={} ", hash);

            return hash;

        } catch (IOException ex) {
            log.error("Exception while storing file in IPFS", ex);
            throw new TechnicalException("Exception while storing file in IPFS: " + ex.getMessage());
        }
    }

    @Override
    public byte[] getContent(String hash) throws TimeoutException {

        log.debug("Get file in IPFS [hash: {}] ", hash);

        // Validation
        if (StringUtils.isEmpty(hash)) throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            
            Future<byte[]> ipfsFetcherResult = pool.submit(new IPFSFetcher(ipfs, filePointer));
            
            byte[] content = ipfsFetcherResult.get(timeout, TimeUnit.MILLISECONDS);
       
            log.debug("Get file in IPFS [hash: {}]", hash);

            return content;

        } catch (java.util.concurrent.TimeoutException ex ) {
            log.error("Timeout Exception while getting file in IPFS [hash: {}]", hash, ex);
            throw new TimeoutException("Timeout Exception while getting file in IPFS [hash: "+hash+"]");

        } catch (InterruptedException ex) {
            log.error("Interrupted Exception while getting file in IPFS [hash: {}]", hash, ex);
            throw new TechnicalException("Interrupted Exception while getting file in IPFS [hash: "+hash+"]", ex);
            
        } catch (ExecutionException ex) {
            log.error("Execution Exception while getting file in IPFS [hash: {}]", hash, ex);
            throw new TechnicalException("Execution Exception while getting file in IPFS [hash: "+hash+"]", ex);
        }
    }

}
