package net.consensys.mahuta.core.service.storage.ipfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Sets;

import io.ipfs.api.IPFS;
import io.ipfs.api.IPFS.PinType;
import io.ipfs.api.JSONParser;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.Multipart;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multiaddr.MultiAddress;
import io.ipfs.multihash.Multihash;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.service.pinning.PinningService;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.utils.ValidatorUtils;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Slf4j
public class IPFSService implements StorageService, PinningService {

    private final IPFSSettings settings;
    private IPFS ipfs;
    private RetryPolicy<Object> retryPolicy;
    private @Getter Set<PinningService> replicaSet;

    private IPFSService(IPFSSettings settings, IPFS ipfs) {
        ValidatorUtils.rejectIfNull("settings", settings);
        ValidatorUtils.rejectIfNull("ipfs", ipfs);

        this.settings = settings;
        this.ipfs = ipfs;
        this.replicaSet = Sets.newHashSet(this); // IPFSService is a PinningService
        this.configureRetry(2);
        this.configureTimeout(settings.getTimeout());
    }

    public static IPFSService connect() {
        return connect(IPFSSettings.DEFAULT_HOST, IPFSSettings.DEFAULT_PORT);
    }

    public static IPFSService connect(String host, Integer port) {
        ValidatorUtils.rejectIfEmpty("host", host);
        ValidatorUtils.rejectIfNegative("port", port);
        return connect(IPFSSettings.DEFAULT_PROTOCOL, host, port, null);
    }

    public static IPFSService connect(String protocol, String host, Integer port) {
        ValidatorUtils.rejectIfEmpty("host", host);
        ValidatorUtils.rejectIfNegative("port", port);
        ValidatorUtils.rejectIfDifferentThan("protocol", protocol, "http", "https");
        return connect(protocol, host, port, null);
    }

    public static IPFSService connect(String multiaddress) {
        ValidatorUtils.rejectIfEmpty("multiaddress", multiaddress);

        MultiAddress m = new MultiAddress(multiaddress);
        
        return connect(
                multiaddress.contains("https") ? "https":"http", 
                m.getHost(), 
                m.getTCPPort(), 
                multiaddress);
    }

    private static IPFSService connect(String protocol, String host, Integer port, String multiaddress) {
        IPFSSettings settings = IPFSSettings.of(protocol, host, port, multiaddress);

        try {
            IPFS ipfs = Optional.ofNullable(multiaddress).map(IPFS::new).orElseGet(() -> new IPFS(host, port));
            log.info("Connected to ipfs [protocol: {}, host: {}, port: {}, multiaddress: {}]: Node v.{}", protocol, host, port, multiaddress,
                    ipfs.version());

            return new IPFSService(settings, ipfs);

        } catch (Exception ex) {
            String msg = String.format("Error whilst connecting to IPFS [protocol: {}, host: %s, port: %s, multiaddress: %s]", 
                    protocol, host, port, multiaddress);

            log.error(msg, ex);
            throw new ConnectionException(msg, ex);
        }
    }

    public IPFSService configureTimeout(Integer timeout) {
        ValidatorUtils.rejectIfNegative("timeout", timeout);
        this.settings.setTimeout(timeout);
        this.ipfs = this.ipfs.timeout(timeout);
        return this;
    }

    @Deprecated
    public IPFSService configureThreadPool(Integer poolSize) {
        return this;
    }

    public IPFSService configureRetry(Integer maxRetry) {
        return this.configureRetry(maxRetry, Duration.ofSeconds(1));
    }

    public IPFSService configureRetry(Integer maxRetry, Duration delay) {
        ValidatorUtils.rejectIfNegative("maxRetry", maxRetry);
        ValidatorUtils.rejectIfNull("delay", delay);

        this.retryPolicy = new RetryPolicy<>()
                .withDelay(delay)
                .withMaxRetries(maxRetry);
        
        return this;
    }

    public IPFSService addReplica(PinningService pinningService) {
        ValidatorUtils.rejectIfNull("pinningService", pinningService);
        
        this.replicaSet.add(pinningService);
        return this;
    }

    @Override
    public String write(InputStream content, boolean noPin) {
        
        try {
            return this.write(IOUtils.toByteArray(content), noPin);
            
        } catch (IOException ex) {
            log.error("Exception converting Inputstream to byte array", ex);
            throw new TechnicalException("Exception converting Inputstream to byte array", ex);
        }
    }

    @Override
    public String write(byte[] content, boolean noPin) {
        log.debug("Write file on IPFS [noPin: {}]", noPin);

        ValidatorUtils.rejectIfNull("content", content);

        return Failsafe.with(retryPolicy)
                .onFailure(event -> log.error("Exception writing file on IPFS after {} attemps.", event.getAttemptCount()))
                .onSuccess(event -> log.debug("File written on IPFS: [id: {}, noPin: {}] ", event.getResult(), noPin))
                .get(() -> {
                    try {
                        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(content);
                        MerkleNode response = add(file, noPin).get(0);
                        return response.hash.toString();
                    } catch (RuntimeException ex) {
                        log.error("Exception while writing file on IPFS", ex);
                        if(ex.getMessage().contains("timeout")) { //TODO find something more elegant
                            throw new TimeoutException("Exception while writing file on IPFS", ex);
                        } else {
                            throw ex;
                        }
                    } catch (IOException ex) {
                        log.error("Exception while writing file on IPFS", ex);
                        throw new TechnicalException("Exception while writing file on IPFS", ex);
                    }
                });
    }

    @Override
    public void pin(String cid) {
        log.debug("Pin CID {} on IPFS", cid);
        
        ValidatorUtils.rejectIfEmpty("cid", cid);
        
        try {
            Multihash hash = Multihash.fromBase58(cid);
            this.ipfs.pin.add(hash);
            
        } catch (Exception ex) {
            throw new TechnicalException("Exception pinning cid " +cid+ " on IPFS", ex);
        }
    } 

    @Override
    public void unpin(String cid) {
        log.debug("Unpin CID {} on IPFS", cid);
        
        ValidatorUtils.rejectIfEmpty("cid", cid);
        
        try {
            Multihash hash = Multihash.fromBase58(cid);
            this.ipfs.pin.rm(hash);
            
        } catch (Exception ex) {
            throw new TechnicalException("Exception unpinning cid " +cid+ " on IPFS", ex);
        }  
    }
    
    @Override
    public List<String> getTracked() {

        log.debug("Get pinned files on IPFS");
        
        try {
            Map<Multihash, Object> cids = this.ipfs.pin.ls(PinType.all);

            return cids.entrySet().stream()
                    .map(e-> e.getKey().toBase58())
                    .collect(Collectors.toList());
            
        } catch (Exception ex) {
            throw new TechnicalException("Exception getting pinned files on IPFS", ex);
        } 
    }

    @Override
    public OutputStream read(String id) {
        return read(id, new ByteArrayOutputStream());
    }

    @Override
    public OutputStream read(String id, OutputStream output) {
        log.debug("Read file on IPFS [id: {}]", id);
       
        return Failsafe.with(retryPolicy)
                .onFailure(event -> log.error("Exception reading file [id: {}] on IPFS after {} attempts.", id, event.getAttemptCount(), event.getFailure()))
                .onSuccess(event -> log.debug("File read on IPFS: [id: {}] ", id))
                .get(() -> {
                    try {
                        Multihash multihash = Multihash.fromBase58(id);
                        byte[] content =  this.ipfs.cat(multihash);
                        IOUtils.write(content, output);

                        return output;
                    } catch (RuntimeException ex) {
                        log.error("Exception while fetching file from IPFS [id: {}]", id, ex);
                        if(ex.getMessage().contains("timeout")) { //TODO find something more elegant
                            throw new TimeoutException("Exception while fetching file from IPFS [id: {}]", ex);
                        } else {
                            throw ex;
                        }
                    } catch (IOException ex) {
                        log.error("Exception while fetching file from IPFS [id: {}]", id, ex);
                        throw new TechnicalException("Exception while fetching file from IPFS " + id, ex);
                    }
                });
    }

    @Override
    public String getName() {
        return "ipfs node [" + settings.getHost() + ":" + settings.getPort() + "]";
    }

    /**
     * Get IPFS Config
     * @param key
     * @return Config associated to the key
     */
    public Object getPeerConfig(String key) {
        
        try {
            return ipfs.id().get(key);
        } catch (IOException ex) {
            log.error("Exception while fetching config from IPFS [key: {}]", key, ex);
            throw new TechnicalException("Exception while fetching config from IPFS. key:" + key, ex);
        }
    }
    
    /**
     * Special method to add content with extra flag not implemented in java-ipfs
     * @param file Content to add
     * @param noPin Add flag pin=!noPin to the request
     * @return List<MerkleNode>
     * @throws IOException
     */
    private List<MerkleNode> add(NamedStreamable.ByteArrayWrapper file, boolean noPin) throws IOException {
        String url = settings.getProtocol() + "://" + settings.getHost() + ":" + settings.getPort() + "/api/v0/" + "add?stream-channels=true&pin="+!noPin;
        log.trace("url: {}", url);
        Multipart m = new Multipart(url, "UTF-8");
        m.addFilePart("file", Paths.get(""), file);
        String res = m.finish();
        return JSONParser.parseStream(res).stream()
                .map(x -> MerkleNode.fromJSON((Map<String, Object>) x))
                .collect(Collectors.toList());
    }
}
