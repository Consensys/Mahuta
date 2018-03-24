package net.consensys.tools.ipfs.ipfsstore.client.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IPFSStoreClientCLI  {

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(IPFSStoreClientCLI.class);
        app.setWebEnvironment(false); 
        app.run(args);
    }
 
    
}
