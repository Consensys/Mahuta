package net.consensys.tools.ipfs.ipfsstore.dao.pinning;

import static net.consensys.tools.ipfs.ipfsstore.Settings.ERROR_NOT_NULL_OR_EMPTY;

import java.io.IOException;

import org.springframework.util.StringUtils;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.AbstractConfiguration;
import net.consensys.tools.ipfs.ipfsstore.dao.PinningStrategy;
import net.consensys.tools.ipfs.ipfsstore.exception.TechnicalException;

@Slf4j
public class NativePinningStrategy implements PinningStrategy {

    public static final String NAME = "native";

    private final IPFS ipfs;

    public NativePinningStrategy(AbstractConfiguration config) {
        this.ipfs = config.getAdditionalParam("multiaddress")
                .map(multiaddress -> new IPFS(multiaddress))
                .orElseGet(() -> new IPFS(config.getHost(), config.getPort()));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void pin(String hash) {

        log.debug("pin file in IPFS  [hash={}]", hash);

        // Validation
        if (StringUtils.isEmpty(hash))
            throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            this.ipfs.pin.add(filePointer);

            log.debug("File pinned in IPFS [hash=" + hash + "]");

        } catch (IOException ex) {
            log.error("Exception while pinning file in IPFS [hash={}]", hash, ex);
            throw new TechnicalException(
                    "Exception while pinning file in IPFS " + hash + ": " + ex.getMessage());
        }
    }

    @Override
    public void unpin(String hash) {

        log.debug("unpin file in IPFS [hash={}]", hash);

        // Validation
        if (StringUtils.isEmpty(hash))
            throw new IllegalArgumentException("hash " + ERROR_NOT_NULL_OR_EMPTY);

        try {
            Multihash filePointer = Multihash.fromBase58(hash);
            this.ipfs.pin.rm(filePointer);

            log.debug("File unpinned in IPFS [hash={}]", hash);

        } catch (IOException ex) {
            log.error("Exception while pinning file in IPFS [hash={}]", hash, ex);
            throw new TechnicalException(
                    "Exception while pinning file in IPFS " + hash + ex.getMessage());
        }

    }

}
