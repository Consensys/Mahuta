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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Sets;

import io.ipfs.api.IPFS;
import io.ipfs.api.JSONParser;
import io.ipfs.api.IPFS.PinType;
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
    private final IPFS ipfs;
    private ExecutorService pool;
    private RetryPolicy<Object> retryPolicy;
    private @Getter Set<PinningService> replicaSet;

    private IPFSService(IPFSSettings settings, IPFS ipfs) {
        ValidatorUtils.rejectIfNull("settings", settings);
        ValidatorUtils.rejectIfNull("ipfs", ipfs);

        this.settings = settings;
        this.ipfs = ipfs;
        this.replicaSet = Sets.newHashSet(this); // IPFSService is a PinningService
        this.configureThreadPool(10);
        this.configureRetry(3);
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

    public IPFSService configureReadTimeout(Integer readTtimeout) {
        ValidatorUtils.rejectIfNegative("readTtimeout", readTtimeout);
        this.settings.setReadTimeout(readTtimeout);
        return this;
    }

    public IPFSService configureWriteTimeout(Integer writeTimeout) {
        ValidatorUtils.rejectIfNegative("writeTimeout", writeTimeout);
        this.settings.setWriteTimeout(writeTimeout);
        return this;
    }

    public IPFSService configureThreadPool(Integer poolSize) {
        ValidatorUtils.rejectIfNegative("poolSize", poolSize);
        this.pool = Executors.newFixedThreadPool(poolSize);
        return this;
    }

    public IPFSService configureRetry(Integer maxRetry) {
        return this.configureRetry(maxRetry, Duration.ofSeconds(1));
    }

    public IPFSService configureRetry(Integer maxRetry, Duration delay) {
        ValidatorUtils.rejectIfNegative("maxRetry", maxRetry);
        ValidatorUtils.rejectIfNull("delay", delay);

        this.retryPolicy = new RetryPolicy<>()
                .handle(IOException.class)
                .handle(ExecutionException.class)
                .handle(TimeoutException.class)
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
                .onFailure(event -> log.error("Exception writing file on IPFS after {} attemps. {}", event.getAttemptCount(), event.getResult()))
                .onSuccess(event -> log.debug("File written on IPFS: [id: {}, noPin: {}] ", event.getResult(), noPin))
                .get(() -> {
                    try {
                        Future<String> ipfsContentResult = pool.submit(new IPFSContentWritter(ipfs, content, noPin));

                        return ipfsContentResult.get(settings.getWriteTimeout(), TimeUnit.MILLISECONDS);

                    } catch (java.util.concurrent.TimeoutException ex) {
                        log.error("Timeout Exception while writing file on IPFS [timeout: {} ms]", settings.getWriteTimeout());
                        throw new TimeoutException("Timeout Exception while while writing file on IPFS");

                    } catch (InterruptedException ex) {
                        log.error("Interrupted Exception while writing file on IPFS");
                        Thread.currentThread().interrupt();
                        throw new TechnicalException("Interrupted Exception while writing file on IPFS", ex);

                    } catch (ExecutionException ex) {
                        log.error("Execution Exception while writing file on IPFS", ex);
                        throw new TechnicalException("Execution Exception while writing file on IPFS", ex);

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

        ValidatorUtils.rejectIfEmpty("id", id);

        return Failsafe.with(retryPolicy)
                .onFailure(event -> log.error("Exception reading file [id: {}] on IPFS after {} attemps. {}", id, event.getAttemptCount(), event.getResult()))
                .onSuccess(event -> log.debug("File read on IPFS: [id: {}] ", id))
                .get(() -> {
                    try {
                        Multihash filePointer = Multihash.fromBase58(id);

                        Future<byte[]> ipfsFetcherResult = pool.submit(new IPFSContentFetcher(ipfs, filePointer));

                        byte[] content = ipfsFetcherResult.get(settings.getReadTimeout(), TimeUnit.MILLISECONDS);
                        IOUtils.write(content, output);

                        return output;

                    } catch (java.util.concurrent.TimeoutException ex) {
                        log.error("Timeout Exception while fetching file from IPFS [id: {}, timeout: {} ms]", id,
                                settings.getReadTimeout());
                        throw new TimeoutException("Timeout Exception while fetching file from IPFS [id: " + id + "]");

                    } catch (InterruptedException ex) {
                        log.error("Interrupted Exception while fetching file from IPFS [id: {}]", id);
                        Thread.currentThread().interrupt();
                        throw new TechnicalException("Interrupted Exception while fetching file from IPFS [id: " + id + "]", ex);

                    } catch (ExecutionException ex) {
                        log.error("Execution Exception while fetching file from IPFS [id: {}]", id, ex);
                        throw new TechnicalException("Execution Exception while fetching file from IPFS [id: " + id + "]", ex);

                    } catch (IOException ex) {
                        log.error("IOException while fetching file from IPFS [id: {}]", id, ex);
                        throw new TechnicalException("Execution Exception while fetching file from IPFS [id: " + id + "]", ex);
                    }
                });
        
    }

    private class IPFSContentFetcher implements Callable<byte[]> {

        private final IPFS ipfs;
        private final Multihash multihash;

        public IPFSContentFetcher(IPFS ipfs, Multihash multihash) {
            this.ipfs = ipfs;
            this.multihash = multihash;
        }

        @Override
        public byte[] call() {
            try {
                return this.ipfs.cat(multihash);
            } catch (IOException ex) {
                log.error("Exception while fetching file from IPFS [hash: {}]", multihash, ex);
                throw new TechnicalException("Exception while fetching file from IPFS " + multihash, ex);
            }
        }
    }

    private class IPFSContentWritter implements Callable<String> {

        private final IPFS ipfs;
        private final byte[] content;
        private final boolean noPin;

        public IPFSContentWritter(IPFS ipfs, byte[] content, boolean noPin) {
            this.ipfs = ipfs;
            this.content = content;
            this.noPin = noPin;
        }

        @Override
        public String call() {
            try {
                NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(content);
                MerkleNode response = add(file).get(0);
                return response.hash.toString();
            } catch (IOException ex) {
                log.error("Exception while writing file on IPFS", ex);
                throw new TechnicalException("Exception while writing file on IPFS", ex);
            }
        }
        
        private List<MerkleNode> add(NamedStreamable.ByteArrayWrapper file) throws IOException {
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

	@Override
	public String getName() {
		return "ipfs node [" + settings.getHost() + ":" + settings.getPort() + "]";
	}

}
