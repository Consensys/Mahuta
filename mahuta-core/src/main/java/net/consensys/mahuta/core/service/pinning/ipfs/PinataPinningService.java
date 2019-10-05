package net.consensys.mahuta.core.service.pinning.ipfs;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.ipfs.multiaddr.MultiAddress;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.exception.ConnectionException;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.service.pinning.PinningService;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Slf4j
public class PinataPinningService implements PinningService {

	private static final String DEFAULT_ENDPOINT = "https://api.pinata.cloud";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_CONTENT_TYPE_VAL = "application/json";
	private static final String HEADER_API_KEY = "pinata_api_key";
	private static final String HEADER_SECRET_API_KEY = "pinata_secret_api_key";
	private static final ObjectMapper mapper = new ObjectMapper();

	private final String endpoint;
	private final String apiKey;
	private final String secretApiKey;
	private final List<String> addresses;

	private PinataPinningService(String endpoint, String apiKey, String secretApiKey, List<String> addresses) {
		this.endpoint = endpoint;
		this.apiKey = apiKey;
		this.secretApiKey = secretApiKey;
		this.addresses = addresses;
	}

	public static PinataPinningService connect(String apiKey, String secretApiKey) {
		return connect(DEFAULT_ENDPOINT, apiKey, secretApiKey, null);
	}

	public static PinataPinningService connect(String apiKey, String secretApiKey, List<String> addresses) {
		return connect(DEFAULT_ENDPOINT, apiKey, secretApiKey, addresses);
	}

	public static PinataPinningService connect(String endpoint, String apiKey, String secretApiKey, List<String> addresses) {
		ValidatorUtils.rejectIfEmpty("endpoint", endpoint);
		ValidatorUtils.rejectIfEmpty("apiKey", apiKey);
		ValidatorUtils.rejectIfEmpty("secretApiKey", secretApiKey);
		
		if(addresses != null)
			addresses.forEach(MultiAddress::new); // Validate multiAddress

		try {
			log.trace("call GET {}/data/testAuthentication", endpoint);
			HttpResponse<String> response = Unirest.get(endpoint + "/data/testAuthentication")
					.header(HEADER_API_KEY, apiKey)
					.header(HEADER_SECRET_API_KEY, secretApiKey)
					.asString()
					.ifFailure(r -> { throw new UnirestException(r.getStatus() + " - " + r.getBody()); });
			log.info("Connected to Pinata [endpoint: {}, apiKey: {}, secretApiKey: {}, addresses: {}]: Info {}",
					endpoint, apiKey, obfuscateKey(secretApiKey), addresses,
					response.getBody());

			return new PinataPinningService(endpoint, apiKey, secretApiKey, addresses);

		} catch (UnirestException ex) {
			String msg = String.format("Error whilst connecting to Pinata  [endpoint: %s, apiKey: %s, secretApiKey: %s, addresses: %s]",
					endpoint, apiKey, obfuscateKey(secretApiKey), addresses);
			log.error(msg, ex);
			throw new ConnectionException(msg, ex);
		}
	}

    @Override
    public void pin(String cid) {
        log.debug("pin CID {} on Pinata", cid);

        try {
            ValidatorUtils.rejectIfEmpty("cid", cid);
            
            PinataPinRequest request = new PinataPinRequest(cid, addresses, null);
            log.trace("call POST {}/pinning/pinHashToIPFS {}", endpoint, mapper.writeValueAsString(request));
            
            HttpResponse<String> response = Unirest.post(endpoint + "/pinning/pinHashToIPFS")
				.header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VAL)
				.header(HEADER_API_KEY, apiKey)
				.header(HEADER_SECRET_API_KEY, secretApiKey)
            	.body(mapper.writeValueAsString(request))
            	.asString()
				.ifFailure(r -> { throw new UnirestException(r.getStatus() + " - " + r.getBody()); });
			log.trace("response: {}", response.getBody());
            
            log.debug("CID {} pinned on Pinata", cid);
            
        } catch (UnirestException | JsonProcessingException ex) {
            String msg = String.format("Error whilst sending request to Pinata [endpoint: %s/pinning/pinHashToIPFS, cid: %s]", endpoint, cid);
            log.error(msg, ex);
            throw new TechnicalException(msg, ex);
        }
    }
    
    @Override
    public void unpin(String cid) {
        log.debug("unpin CID {} on Pinata", cid);

        try {
            ValidatorUtils.rejectIfEmpty("cid", cid);
            
            PinataUnpinRequest request = new PinataUnpinRequest(cid);
            log.trace("call POST {}/pinning/removePinFromIPFS {}", endpoint, mapper.writeValueAsString(request));
            
            HttpResponse<String> response = Unirest.post(endpoint + "/pinning/removePinFromIPFS")
    			.header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VAL)
				.header(HEADER_API_KEY, apiKey)
				.header(HEADER_SECRET_API_KEY, secretApiKey)
            	.body(mapper.writeValueAsString(request))
            	.asString()
				.ifFailure(r -> { throw new UnirestException(r.getStatus() + " - " + r.getBody()); });
			log.trace("response: {}", response.getBody());
            
            log.debug("CID {} unpinned on Pinata", cid);
            
        } catch (UnirestException | JsonProcessingException ex) {
            String msg = String.format("Error whilst sending request to Pinata [endpoint: %s/pinning/removePinFromIPFS, cid: %s]", endpoint, cid);
            log.error(msg, ex);
            throw new TechnicalException(msg, ex);
        }
    }

	@Override
	public List<String> getTracked() {
		log.debug("get pinned files on Pinata");

		try {
			log.trace("GET {}/data/pinList?status=pinned&pageLimit=1000", endpoint);
			HttpResponse<String> response = Unirest.get(endpoint + "/data/pinList?status=pinned&pageLimit=1000")
					.header(HEADER_API_KEY, apiKey)
					.header(HEADER_SECRET_API_KEY, secretApiKey)
					.asString()
					.ifFailure(r -> { throw new UnirestException(r.getStatus() + " - " + r.getBody()); });
			log.trace("response: {}", response.getBody());

			PinataTrackedResponse result = mapper.readValue(response.getBody(), PinataTrackedResponse.class);
			
			log.debug("get pinned files on Pinata [count: {}]", result.getCount());
			return result.getRows()
					.stream()
					.map(r -> r.getHash())
					.collect(Collectors.toList());

		} catch (UnirestException | IOException ex) {
			log.error("Exception whilst requesting the tracked data", ex);
			throw new TechnicalException("Exception whilst requesting the tracked data", ex);
		}
	}

	@Override
	public String getName() {
		return "Pinata [endpoint: "+endpoint+", apiKey: "+apiKey+"]";
	}
	
	private static String obfuscateKey(String key) {
		ValidatorUtils.rejectIfEmpty("key", key);
		
		if(key.length() < 5) return "*********";
		
		return key.substring(0, 5)+"***********";
	}
}