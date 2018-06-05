package net.consensys.tools.ipfs.ipfsstore.dao.storage;

import static net.consensys.tools.ipfs.ipfsstore.Constant.ERROR_NOT_NULL_OR_EMPTY;

import java.io.IOException;

import org.springframework.util.StringUtils;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;

/**
 * IPFS implementation of StorageDao
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Slf4j
public class IPFSStorageDao implements StorageDao {

    private final IPFS ipfs;

    public IPFSStorageDao(IPFS ipfs) {
        this.ipfs = ipfs;
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
    public String createContent(byte[] content) throws DaoException {

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
            throw new DaoException("Exception while storing file in IPFS: " + ex.getMessage());
        }
    }

    @Override
    public byte[] getContent(String hash) throws DaoException {

        log.debug("Get file in IPFS [hash: {}] ", hash);

        // Validation
        if (StringUtils.isEmpty(hash)) throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            byte[] content = this.ipfs.cat(filePointer);

            log.debug("Get file in IPFS [hash: {}]", hash);

            return content;

        } catch (IOException ex) {
            log.error("Exception while getting file in IPFS [hash: {}]", hash, ex);
            throw new DaoException("Exception while getting file in IPFS " + hash + ex.getMessage());
        }
    }

}
