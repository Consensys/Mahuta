package net.consensys.tools.ipfs.ipfsstore.configuration;

import static net.consensys.tools.ipfs.ipfsstore.Settings.ERROR_NOT_NULL_OR_EMPTY;
import static net.consensys.tools.ipfs.ipfsstore.Settings.STORAGE_IPFS;
import static net.consensys.tools.ipfs.ipfsstore.Settings.STORAGE_SWARM;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

import io.ipfs.api.IPFS;
import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.health.HealthCheckScheduler;
import net.consensys.tools.ipfs.ipfsstore.dao.StorageDao;
import net.consensys.tools.ipfs.ipfsstore.dao.storage.IPFSStorageDao;
import net.consensys.tools.ipfs.ipfsstore.exception.ConnectionException;

/**
 * Configuration for the storage layer
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ipfs-store.storage")
@DependsOn("HealthCheckScheduler")
@Slf4j
public class StorageConfiguration extends AbstractConfiguration {

    private HealthCheckScheduler healthCheckScheduler;

    @Autowired
    public StorageConfiguration(HealthCheckScheduler healthCheckScheduler) {
        this.healthCheckScheduler = healthCheckScheduler;
    }

    /**
     * Load the IPFS Storage Dao bean up
     * 
     * @return IPFS StorageDAO bean
     */
    @Bean
    @ConditionalOnProperty(value = "ipfs-store.storage.type", havingValue = STORAGE_IPFS)
    public StorageDao ipfsStorageDao() throws ConnectionException {

        log.info("Connecting to IPFS [{}]", super.toString());

        IPFS ipfs = super.getAdditionalParam("multiaddress")
                .map(multiaddress -> new IPFS(multiaddress))
                .orElseGet(() -> new IPFS(host, port));

        StorageDao bean = new IPFSStorageDao(this, ipfs);

        // Register to the heath check service
        healthCheckScheduler.registerHealthCheck("ipfs", bean);

        log.info("Connected to IPFS [{}]", super.toString());
        // log.debug(ipfs.config.show().toString());

        return bean;

    }

    /**
     * Load the Swarm Storage Dao bean up
     * 
     * @return Swarm StorageDAO bean
     */
    @Bean
    @ConditionalOnProperty(value = "ipfs-store.storage.type", havingValue = STORAGE_SWARM)
    public StorageDao swarmStorageDao() throws ConnectionException {
        throw new UnsupportedOperationException("Swarm StorageDAO is not implemented yet !");
    }

}
