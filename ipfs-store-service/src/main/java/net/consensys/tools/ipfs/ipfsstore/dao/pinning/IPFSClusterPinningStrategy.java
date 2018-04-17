package net.consensys.tools.ipfs.ipfsstore.dao.pinning;

import static net.consensys.tools.ipfs.ipfsstore.Constant.ERROR_NOT_NULL_OR_EMPTY;
import static net.consensys.tools.ipfs.ipfsstore.Constant.printHash;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.AbstractConfiguration;
import net.consensys.tools.ipfs.ipfsstore.dao.PinningStrategy;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;

@Slf4j
public class IPFSClusterPinningStrategy implements PinningStrategy {
  
  public static final String NAME = "ipfs_cluster";
  
  private final String host;
  private final Integer port;
  private final RestTemplate restTemplate;

  public IPFSClusterPinningStrategy(AbstractConfiguration config) {    
    this.restTemplate = new RestTemplate();
    this.host = config.getHost();
    this.port = config.getPort();
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public void pin(String hash) throws DaoException {
    log.debug("pin file in IPFS-cluster  [hash={}]", hash);

    // Validation
    if (StringUtils.isEmpty(hash)) throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

    try {
      log.debug("call POST http://"+host+":"+port+"/pins/"+hash);
      this.restTemplate.postForLocation("http://"+host+":"+port+"/pins/"+hash, null);

        log.debug("File pinned in IPFS-cluster [hash=" + hash + "]");

    } catch (Exception ex) {
        log.error("Exception while pinning file in IPFS-cluster [hash={}]", hash, ex);
        throw new DaoException("Exception while pining file in IPFS-cluster " + printHash(hash) + ": " + ex.getMessage());
    }
  }

  @Override
  public void unpin(String hash) throws DaoException {
    log.debug("unpin file in IPFS-cluster [hash={}]", hash);

    // Validation
    if (StringUtils.isEmpty(hash)) throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

    try {
        // TODO Auto-generated method stub

        log.debug("File unpinned in IPFS-cluster [hash={}]", hash);

    } catch (Exception ex) {
        log.error("Exception while pinning file in IPFS-cluster [hash={}]", hash, ex);
        throw new DaoException("Exception while pining file in IPFS-cluster " + printHash(hash) + ex.getMessage());
    }
  }

}
