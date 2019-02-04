package net.consensys.mahuta.core.service.storage.ipfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import io.ipfs.api.IPFS;
import io.ipfs.api.IPFS.PinType;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.exception.TimeoutException;
import net.consensys.mahuta.core.service.storage.StorageService;
import net.consensys.mahuta.core.utils.ValidatorUtils;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Slf4j
public class IPFSService implements StorageService {

    private final IPFSSettings settings;
    private final IPFS ipfs;
    private ExecutorService pool;
    private RetryPolicy<Object> retryPolicy;

    private IPFSService(IPFSSettings settings, IPFS ipfs) {
        ValidatorUtils.rejectIfNull("settings", settings);
        ValidatorUtils.rejectIfNull("ipfs", ipfs);

        this.settings = settings;
        this.ipfs = ipfs;
        this.configureThreadPool(10);
        this.configureRetry(0);
    }

    public static IPFSService connect() {
        return connect(IPFSSettings.DEFAULT_HOST, IPFSSettings.DEFAULT_PORT);
    }

    public static IPFSService connect(String host, Integer port) {
        ValidatorUtils.rejectIfEmpty("host", host);
        ValidatorUtils.rejectIfNegative("port", port);
        return connect(host, port, null);
    }

    public static IPFSService connect(String multiaddress) {
        ValidatorUtils.rejectIfEmpty("multiaddress", multiaddress);

        return connect(null, null, multiaddress);
    }

    private static IPFSService connect(String host, Integer port, String multiaddress) {
        IPFSSettings settings = IPFSSettings.of(host, port, multiaddress);

        try {
            IPFS ipfs = Optional.ofNullable(multiaddress).map(IPFS::new).orElseGet(() -> new IPFS(host, port));
            log.info("Connected to ipfs [host: {}, port: {}, multiaddress: {}]: Node v.{}", host, port, multiaddress,
                    ipfs.version());

            return new IPFSService(settings, ipfs);

        } catch (Exception ex) {
            String msg = String.format("Error whilst connecting to IPFS [host: %s, port: %s, multiaddress: %s]", host,
                    port, multiaddress);

            log.error(msg, ex);
            throw new ConnectionException(msg, ex);
        }
    }

    public IPFSService configureTimeout(Integer timeout) {
        ValidatorUtils.rejectIfNegative("timeout", timeout);
        this.settings.setTimeout(timeout);
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
                .withDelay(delay)
                .withMaxRetries(maxRetry);
        
        return this;
    }

    @Override
    public String write(InputStream content) {
        
        try {
            return this.write(IOUtils.toByteArray(content));
            
        } catch (Exception ex) {
            log.error("Exception converting Inputstream to byte array", ex);
            throw new TechnicalException("Exception converting Inputstream to byte array", ex);
        }
    }

    @Override
    public String write(byte[] content) {
        log.debug("Write file on IPFS");

        ValidatorUtils.rejectIfNull("content", content);

        return Failsafe.with(retryPolicy)
            .onFailure(event -> log.error("Exception writting file on IPFS after {} attemps", event.getAttemptCount(), event.getResult()))
            .onSuccess(event -> log.debug("File written on IPFS: hash={} ", event.getResult()))
            .get(() -> {
                NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(content);
                MerkleNode response = this.ipfs.add(file).get(0);
                return response.hash.toString();
            });
    }

    @Override
    public void pin(String cid) {

        try {
            log.debug("Pin CID {} on IPFS", cid);
            
            ValidatorUtils.rejectIfEmpty("cid", cid);

            Multihash hash = Multihash.fromBase58(cid);
            this.ipfs.pin.add(hash);

            log.debug("CID {} pinned on IPFS", cid);

        } catch (IOException ex) {
            log.error("Exception pinning CID {} on IPFS", cid, ex);
            throw new TechnicalException("Exception pinning CID " + cid + " on IPFS", ex);
        }
    }

    @Override
    public void unpin(String cid) {
        try {
            log.debug("Unpin CID {} on IPFS", cid);
            
            ValidatorUtils.rejectIfEmpty("cid", cid);

            Multihash hash = Multihash.fromBase58(cid);
            this.ipfs.pin.rm(hash);

            log.debug("CID {} unpinned on IPFS", cid);

        } catch (IOException ex) {
            log.error("Exception unpinning CID {} on IPFS", cid, ex);
            throw new TechnicalException("Exception unpinning CID " + cid + " on IPFS", ex);
        }
    }
    
    @Override
    public List<String> getPinned() {
        try {
            log.debug("Get pinned files on IPFS");
            
            Map<Multihash, Object> cids = this.ipfs.pin.ls(PinType.all);

            log.debug("Get pinned files on IPFS: {}", cids);

            return cids.entrySet().stream()
                    .map(e-> e.getKey().toBase58())
                    .collect(Collectors.toList());
            
        } catch (IOException ex) {
            log.error("Exception getting pinned files on IPFS", ex);
            throw new TechnicalException("Exception getting pinned files on IPFS", ex);
        }
    }

    @Override
    public OutputStream read(String id) {
        return read(id, new ByteArrayOutputStream());
    }

    @Override
    public OutputStream read(String id, OutputStream output) {

        try {
            log.debug("Read file on IPFS [id: {}]", id);

            ValidatorUtils.rejectIfEmpty("id", id);

            Multihash filePointer = Multihash.fromBase58(id);

            Future<byte[]> ipfsFetcherResult = pool.submit(new IPFSContentFetcher(ipfs, filePointer));

            byte[] content = ipfsFetcherResult.get(settings.getTimeout(), TimeUnit.MILLISECONDS);
            IOUtils.write(content, output);

            log.debug("File read on IPFS [id: {}]", id);

            return output;

        } catch (java.util.concurrent.TimeoutException ex) {
            log.error("Timeout Exception while fetching file from IPFS [id: {}, timeout: {} ms]", id,
                    settings.getTimeout());
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
}
