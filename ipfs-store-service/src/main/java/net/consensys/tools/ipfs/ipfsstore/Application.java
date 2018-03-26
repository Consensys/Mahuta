package net.consensys.tools.ipfs.ipfsstore;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

/**
 * Spring Boot entry point
 */
@SpringBootApplication
@Profile("default")
@ComponentScan({"net.consensys.tools.ipfs.ipfsstore"})
public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class);
    private static final String NAME = "IPFS-STORE";

    public static void main(String[] args) {
        LOGGER.info("############### Starting Application [" + NAME + "] ... ############### ");
        SpringApplication.run(Application.class, args);
        LOGGER.info("############### Application [" + NAME + "] Started ! ############### ");
    }
}
