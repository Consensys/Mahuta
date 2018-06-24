package net.consensys.tools.ipfs.ipfsstore.configuration;

import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AbstractConfiguration {

    protected boolean enable = true;
    protected String id;
    protected String type;
    protected String host;
    protected Integer port;
    protected Map<String, String> additional;

    public Optional<String> getAdditionalParam(String key) {
        if (additional == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(additional.get(key));
    }

    @Override
    public String toString() {
        return "[enable=" + enable + ", id=" + id + ", type=" + type + ", host=" + host + ", port="
                + port + ", additional=" + additional + "]";
    }

}
