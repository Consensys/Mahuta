package net.consensys.tools.ipfs.ipfsstore;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

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

    @SuppressWarnings("rawtypes")
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        log.trace("====== Environment and configuration ======");
        log.trace("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false).filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames()).flatMap(Arrays::stream).distinct()
                .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
                .forEach(prop -> log.trace("{}: {}", prop, env.getProperty(prop)));
        log.trace("===========================================");
    }

}
