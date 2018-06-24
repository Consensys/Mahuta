package net.consensys.tools.ipfs.ipfsstore.configuration;

import static net.consensys.tools.ipfs.ipfsstore.Settings.ERROR_NOT_NULL_OR_EMPTY;
import static net.consensys.tools.ipfs.ipfsstore.Settings.INDEXER_ELASTICSEARCH;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.consensys.tools.ipfs.ipfsstore.configuration.health.HealthCheckScheduler;
import net.consensys.tools.ipfs.ipfsstore.dao.IndexDao;
import net.consensys.tools.ipfs.ipfsstore.dao.index.ElasticSearchIndexDao;
import net.consensys.tools.ipfs.ipfsstore.exception.ConnectionException;

/**
 * Configuration for the indexer layer
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ipfs-store.index")
@DependsOn("HealthCheckScheduler")
@Slf4j
public class IndexerConfiguration extends AbstractConfiguration {

    private HealthCheckScheduler healthCheckScheduler;

    @Autowired
    public IndexerConfiguration(HealthCheckScheduler healthCheckScheduler) {
        this.healthCheckScheduler = healthCheckScheduler;
    }

    /**
     * Load the ElasticSearch Indexer Dao bean up
     * 
     * @return ElasticSearch IndexerDAO bean
     */
    @Bean
    @ConditionalOnProperty(value = "ipfs-store.index.type", havingValue = INDEXER_ELASTICSEARCH)
    public IndexDao elacticSearchIndexDao() throws ConnectionException {

        final String KEY_CLUSTER = "clusterName";
        final String KEY_INDEX_NULL = "indexNullValue";

        if (StringUtils.isEmpty(host))
            throw new IllegalArgumentException("ipfs-store.index.host" + ERROR_NOT_NULL_OR_EMPTY);
        if (StringUtils.isEmpty(port))
            throw new IllegalArgumentException("ipfs-store.index.host" + ERROR_NOT_NULL_OR_EMPTY);
        if (!this.getAdditionalParam(KEY_CLUSTER).isPresent())
            throw new IllegalArgumentException(
                    "ipfs-store.index.additional.clusterName" + ERROR_NOT_NULL_OR_EMPTY);

        try {
            log.info("Connecting to ElasticSearch [host: {}, port: {}, cluster: {}]", host, port,
                    additional.get(KEY_CLUSTER));

            // Load the client and start the connection
            PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(
                    Settings.builder().put("cluster.name", additional.get(KEY_CLUSTER)).build());

            TransportClient transportClient = preBuiltTransportClient.addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(host), port));

            IndexDao bean = new ElasticSearchIndexDao(preBuiltTransportClient, transportClient,
                    Boolean.parseBoolean(additional.get(KEY_INDEX_NULL)));

            // Register to the heath check service
            healthCheckScheduler.registerHealthCheck("elasticsearch", bean);

            log.info("Connected to ElasticSearch [host: {}, port: {}, cluster: {}] : {}", host,
                    port, additional.get(KEY_CLUSTER), transportClient.listedNodes());

            return bean;

        } catch (UnknownHostException ex) {
            log.error("Error while connecting to ElasticSearch [host: {}, port: {}, cluster: {}]",
                    host, port, additional.get(KEY_CLUSTER), ex);
            throw new ConnectionException("Error while connecting to ElasticSearch", ex);
        }
    }
}
