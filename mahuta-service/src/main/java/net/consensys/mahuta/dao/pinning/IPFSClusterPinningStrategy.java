package net.consensys.mahuta.dao.pinning;

import static net.consensys.mahuta.Settings.ERROR_NOT_NULL_OR_EMPTY;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.configuration.AbstractConfiguration;
import net.consensys.mahuta.dao.PinningStrategy;
import net.consensys.mahuta.exception.TechnicalException;

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
    public void pin(String hash) {
        log.debug("pin file in IPFS-cluster  [hash={}]", hash);

        // Validation
        if (StringUtils.isEmpty(hash))
            throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            log.debug("call POST http://{}:{}/pins/{}", host, port, hash);
            this.restTemplate.postForLocation("http://" + host + ":" + port + "/pins/" + hash,
                    null);

            log.debug("File pinned in IPFS-cluster [hash={}]", hash);

        } catch (Exception ex) {
            log.error("Exception while pinning file in IPFS-cluster [hash={}]", hash, ex);
            throw new TechnicalException(
                    "Exception while pining file in IPFS-cluster " + hash + ": " + ex.getMessage());
        }
    }

    @Override
    public void unpin(String hash) {
        log.debug("unpin file in IPFS-cluster [hash={}]", hash);

        // Validation
        if (StringUtils.isEmpty(hash))
            throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            log.debug("call DELETE http://{}:{}/pins/{}", host, port, hash);
            this.restTemplate.delete("http://" + host + ":" + port + "/pins/" + hash);

            log.debug("File unpinned in IPFS-cluster [hash={}]", hash);

        } catch (Exception ex) {
            log.error("Exception while pinning file in IPFS-cluster [hash={}]", hash, ex);
            throw new TechnicalException(
                    "Exception while pining file in IPFS-cluster " + hash + ex.getMessage());
        }
    }

}
