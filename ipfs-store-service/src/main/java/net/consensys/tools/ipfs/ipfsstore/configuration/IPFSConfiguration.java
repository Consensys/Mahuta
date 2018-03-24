package net.consensys.tools.ipfs.ipfsstore.configuration;

import java.io.IOException;

import net.consensys.tools.ipfs.ipfsstore.exception.ConnectionException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.ipfs.api.IPFS;

/**
 * Configuration for IPFS
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
public class IPFSConfiguration {

    private static final Logger LOGGER = Logger.getLogger(IPFSConfiguration.class);

    @Value("${ipfs.host}")
    private String ipfsHost;

    @Value("${ipfs.port}")
    private int ipfsPort;

    @Bean
    public IPFS ipfs() throws ConnectionException {

        try {
            LOGGER.info("Connecting to IPFS " + printIPFS(ipfsHost, ipfsPort));

            IPFS ipfs = new IPFS(ipfsHost, ipfsPort);
            ipfs.refs.local();

            LOGGER.info("Connected to IPFS " + printIPFS(ipfsHost, ipfsPort));

            return ipfs;

        } catch (IOException ex) {
            LOGGER.error("Error while connecting to IPFS " + printIPFS(ipfsHost, ipfsPort));
            throw new ConnectionException("Error while connecting to IPFS", ex);
        }
    }

    private String printIPFS(String ipfsHost, int ipfsPort) {
        return "[host: " + ipfsHost + ", port: " + ipfsPort + "]";
    }
}
