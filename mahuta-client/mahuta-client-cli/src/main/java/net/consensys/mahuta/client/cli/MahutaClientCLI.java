package net.consensys.mahuta.client.cli;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MahutaClientCLI {

    public static void main(String... args) {
        new SpringApplicationBuilder(MahutaClientCLI.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
