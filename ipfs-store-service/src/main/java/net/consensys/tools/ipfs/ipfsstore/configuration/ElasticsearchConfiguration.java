package net.consensys.tools.ipfs.ipfsstore.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ElasticSearch
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Configuration
public class ElasticsearchConfiguration implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {

    private static final Logger LOGGER = Logger.getLogger(ElasticsearchConfiguration.class);

    @Value("${elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Value("${elasticsearch.cluster-name}")
    private String clusterName;

    private TransportClient transportClient;

    @Override
    public void destroy() {
        try {
            LOGGER.info("Closing ElasticSearch client");
            if (transportClient != null) {
                transportClient.close();
            }

        } catch (final Exception e) {
            LOGGER.error("Error closing ElasticSearch client: ", e);
        }
    }

    @Override
    public TransportClient getObject() {
        return transportClient;
    }

    @Override
    public Class<TransportClient> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() {
        buildClient();
    }

    /**
     * Generate a TransportClient object that can be used to access the ElasticSearch Java native API
     */
    protected void buildClient() {
        LOGGER.info("Connecting to ElasticSearch [clusterNodes: " + clusterNodes + "]");

        try (PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(settings())) {
            String[] inetSocket = clusterNodes.split(":");
            String address = inetSocket[0];
            Integer port = Integer.valueOf(inetSocket[1]);
            transportClient = preBuiltTransportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));

            LOGGER.info("Connected to ElasticSearch [clusterNodes: " + clusterNodes + "] : " + transportClient.listedNodes());

        } catch (UnknownHostException e) {
            LOGGER.error("Error while connecting to ElasticSearch [clusterNodes: " + clusterNodes + "]", e);
        }
    }

    /**
     * Instantiate a Settings objects with the necessary settings key/values
     *
     * @return ElasticSearch Settings
     */
    private Settings settings() {
        return Settings.builder()
                .put("cluster.name", clusterName)
                //TODO more settings
                .build();
    }
}