package net.consensys.tools.ipfs.ipfsstore.client.cli;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class IPFSStoreClientCLI {

    public static void main(String... args) {
        new SpringApplicationBuilder(IPFSStoreClientCLI.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
