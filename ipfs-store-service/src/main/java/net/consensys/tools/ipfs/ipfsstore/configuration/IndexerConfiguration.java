package net.consensys.tools.ipfs.ipfsstore.configuration;

import static net.consensys.tools.ipfs.ipfsstore.Constant.ERROR_NOT_NULL_OR_EMPTY;
import static net.consensys.tools.ipfs.ipfsstore.Constant.INDEXER_ELASTICSEARCH;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class IndexerConfiguration extends AbstractConfiguration {
  
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
    if (additional == null || !additional.containsKey(KEY_CLUSTER)
        || StringUtils.isEmpty(additional.get(KEY_CLUSTER)))
      throw new IllegalArgumentException("ipfs-store.index.additional.clusterName" + ERROR_NOT_NULL_OR_EMPTY);

    try {
      log.info("Connecting to ElasticSearch [host: {}, port: {}, cluster: {}]", host, port, additional.get(KEY_CLUSTER));

      PreBuiltTransportClient preBuiltTransportClient = new PreBuiltTransportClient(
          Settings.builder().put("cluster.name", additional.get(KEY_CLUSTER)).build());

      TransportClient transportClient = preBuiltTransportClient.addTransportAddress(
          new InetSocketTransportAddress(InetAddress.getByName(host), port));
      
      log.info("Connected to ElasticSearch [host: {}, port: {}, cluster: {}] : {}", host, port, additional.get(KEY_CLUSTER), transportClient.listedNodes());

      return new ElasticSearchIndexDao(preBuiltTransportClient, transportClient, Boolean.parseBoolean(additional.get(KEY_INDEX_NULL)));
      
    } catch (UnknownHostException ex) {
      log.error("Error while connecting to ElasticSearch [host: {}, port: {}, cluster: {}]", host, port, additional.get(KEY_CLUSTER), ex);
      throw new ConnectionException("Error while connecting to ElasticSearch", ex);
    }
  }
}
