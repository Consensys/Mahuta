package net.consensys.tools.ipfs.ipfsstore.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.dao.PinningStrategy;
import net.consensys.tools.ipfs.ipfsstore.dao.pinning.IPFSClusterPinningStrategy;
import net.consensys.tools.ipfs.ipfsstore.dao.pinning.NativePinningStrategy;

/**
 * Configuration for the Pinning layer
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
@ConfigurationProperties(prefix = "ipfs-store.pinning")
@EnableConfigurationProperties
@Slf4j
public class PinningConfiguration {

    private final static Map<String, Function<AbstractConfiguration, PinningStrategy>> executors = new HashMap<>();

    @Setter
    @Getter
    private List<AbstractConfiguration> strategies;
    @Getter
    private List<PinningStrategy> pinningStrategies = new ArrayList<>();

    public PinningConfiguration() {
    }

    @PostConstruct
    public void init() {

        // Configure each pinning strategy instantiation specifications
        executors.put(NativePinningStrategy.NAME, (config) -> {
            return new NativePinningStrategy(config);
        });
        executors.put(IPFSClusterPinningStrategy.NAME, (config) -> {
            return new IPFSClusterPinningStrategy(config);
        });

        // Register each pinning service
        if (strategies == null || strategies.size() == 0) {
            log.warn("No pinning strategy configured");
            return;
        }
        log.debug(strategies.toString());

        strategies.forEach(serviceConfig -> this.registerPinningStrategy(serviceConfig.getType(),
                serviceConfig));

    }

    /**
     * Register a pinning strategy
     * 
     * @param name
     *            Name of the pinning strategy
     * @param config
     *            Configuration associated to the strategy
     */
    public void registerPinningStrategy(String name, AbstractConfiguration config) {

        if (!config.enable) {
            return;
        }

        log.info("Register Pinning Strategy (name: {}, config: {})", name, config);

        if (!executors.containsKey(name)) {
            throw new IllegalArgumentException("Unknow pinning stragtegy name [" + name + "]");
        }

        pinningStrategies.add(executors.get(name).apply(config));
    }

}
