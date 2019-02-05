package net.consensys.mahuta.core.service.pinning.ipfs;

import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.service.pinning.PinningService;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Slf4j
public class IPFSClusterPinningService  implements PinningService {

    private static final String BASE_URI = "http://%s:%s/";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9094;
    
    private final String host;
    private final Integer port;

    private IPFSClusterPinningService(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public static IPFSClusterPinningService connect() {
        return connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static IPFSClusterPinningService connect(String host, Integer port) {
        ValidatorUtils.rejectIfEmpty("host", host);
        ValidatorUtils.rejectIfNegative("port", port);
        
        try {
            log.trace("call GET http://{}:{}/id", host, port);
            HttpResponse<String> response = Unirest.get(String.format(BASE_URI + "/id", host, port)).asString();
            log.info("Connected to IPFS-Cluster [host: {}, port: {}]: Info {}", host, port, response.getBody());
            
            return new IPFSClusterPinningService(host, port);
            
        } catch (UnirestException ex) {
            String msg = String.format("Error whilst connecting to IPFS-Cluster [host: %s, port: %s]", host, port);
            log.error(msg, ex);
            throw new ConnectionException(msg, ex);
        }
    }
    @Override
    public void pin(String cid) {
        log.debug("pin CID {} on IPFS-cluster", cid);

        ValidatorUtils.rejectIfEmpty("cid", cid);
        
        log.trace("call POST http://{}:{}/pins/{}", host, port, cid);
        Unirest.post(String.format(BASE_URI + "/pins/%s", host, port, cid));

        log.debug("CID {} pinned on IPFS-cluster", cid);
    }

    @Override
    public void unpin(String cid) {
        log.debug("unpin CID {} on IPFS-cluster", cid);

        ValidatorUtils.rejectIfEmpty("cid", cid);

        log.trace("call DELETE http://{}:{}/pins/{}", host, port, cid);
        Unirest.delete(String.format(BASE_URI + "/pins/%s", host, port, cid));

        log.debug("unpin {} pinned on IPFS-cluster", cid);
    }

    @Override
    public List<String> getTracked() {
        log.debug("get pinned files on IPFS-cluster");
        
        try {

            log.trace("GET GET http://{}:{}/pins", host, port);
            HttpResponse<JsonNode> response = Unirest.get(String.format(BASE_URI + "/pins", host, port))
                .asJson();

            response.getBody().getArray();
            //TODO
            log.debug("get pinned files on IPFS-cluster");
            return null;
            
        } catch (UnirestException ex) {
            log.error("Exception converting HTTP response to JSON", ex);
            throw new TechnicalException("Exception converting HTTP response to JSON", ex);
        }
    }

}