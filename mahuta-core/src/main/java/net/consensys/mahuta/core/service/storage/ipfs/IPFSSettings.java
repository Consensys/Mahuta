package net.consensys.mahuta.core.service.storage.ipfs;

import lombok.Getter;
import lombok.Setter;

public class IPFSSettings {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5001;
    public static final int DEFAULT_TIMEOUT = 10000;

    private @Setter @Getter String host = DEFAULT_HOST;
    private @Setter @Getter Integer port = DEFAULT_PORT;
    private @Setter @Getter String multiaddress;
    private @Setter @Getter int timeout = DEFAULT_TIMEOUT;
    
    public static IPFSSettings of(String host, Integer port, String multiaddress) {
        IPFSSettings s = new IPFSSettings();
        s.setHost(host);
        s.setPort(port);
        s.setMultiaddress(multiaddress);
        return s;
    }
    
}
