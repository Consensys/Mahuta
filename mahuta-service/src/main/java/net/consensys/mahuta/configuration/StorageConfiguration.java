package net.consensys.mahuta.configuration;

import static net.consensys.mahuta.Settings.STORAGE_IPFS;
import static net.consensys.mahuta.Settings.STORAGE_SWARM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import io.ipfs.api.IPFS;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.configuration.health.HealthCheckScheduler;
import net.consensys.mahuta.dao.StorageDao;
import net.consensys.mahuta.dao.storage.IPFSStorageDao;
import net.consensys.mahuta.exception.ConnectionException;

/**
 * Configuration for the storage layer
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mahuta.storage")
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
    @ConditionalOnProperty(value = "mahuta.storage.type", havingValue = STORAGE_IPFS)
    public StorageDao ipfsStorageDao() throws ConnectionException {

        log.info("Connecting to IPFS [{}]", super.toString());

        IPFS ipfs = super.getAdditionalParam("multiaddress")
                .map(IPFS::new)
                .orElseGet(() -> new IPFS(host, port));

        StorageDao bean = new IPFSStorageDao(this, ipfs);

        // Register to the heath check service
        healthCheckScheduler.registerHealthCheck("ipfs", bean);

        log.info("Connected to IPFS [{}]", super.toString());

        return bean;

    }

    /**
     * Load the Swarm Storage Dao bean up
     * 
     * @return Swarm StorageDAO bean
     */
    @Bean
    @ConditionalOnProperty(value = "mahuta.storage.type", havingValue = STORAGE_SWARM)
    public StorageDao swarmStorageDao() throws ConnectionException {
        throw new UnsupportedOperationException("Swarm StorageDAO is not implemented yet !");
    }

    
}
