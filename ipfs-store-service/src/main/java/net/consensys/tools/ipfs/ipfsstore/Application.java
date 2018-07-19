package net.consensys.tools.ipfs.ipfsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot entry point
 */
@SpringBootApplication
@Profile("default")
@ComponentScan({ "net.consensys.tools.ipfs.ipfsstore" })
@Slf4j
public class Application {

    private static final String NAME = "IPFS-STORE";

    public static void main(String[] args) {
        log.info("############### Starting Application [{}] ... ############### ", NAME);
        SpringApplication.run(Application.class, args);
        log.info("############### Application [{}] Started ! ############### ", NAME);
    }

}
