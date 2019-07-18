package net.consensys.mahuta.core.service.storage.ipfs;

import lombok.Getter;
import lombok.Setter;

public class IPFSSettings {

    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5001;
    public static final int DEFAULT_READ_TIMEOUT = 5000;
    public static final int DEFAULT_WIRTE_TIMEOUT = 5000;

    private @Setter @Getter String protocol = DEFAULT_PROTOCOL;
    private @Setter @Getter String host = DEFAULT_HOST;
    private @Setter @Getter Integer port = DEFAULT_PORT;
    private @Setter @Getter String multiaddress;
    private @Setter @Getter int readTimeout = DEFAULT_READ_TIMEOUT;
    private @Setter @Getter int writeTimeout = DEFAULT_WIRTE_TIMEOUT;
    
    public static IPFSSettings of(String protocol, String host, Integer port, String multiaddress) {
        IPFSSettings s = new IPFSSettings();
        s.setProtocol(protocol);
        s.setHost(host);
        s.setPort(port);
        s.setMultiaddress(multiaddress);
        return s;
    }
    
}
