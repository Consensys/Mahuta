package net.consensys.mahuta.api.http.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mahuta")
public class MahutaSettings {
    private @Setter @Getter IPFS ipfs;
    private @Setter @Getter ElasticSearch elasticSearch;
    
    public static class IPFS {
        private @Getter @Setter String host;
        private @Getter @Setter int port;
        private @Getter @Setter String multiaddress;
        private @Getter @Setter int timeout;
        private @Getter @Setter int threadPool;
    }

    public static class ElasticSearch {
        private @Getter @Setter String host;
        private @Getter @Setter int port;
        private @Getter @Setter String clusterName;
        private @Getter @Setter boolean indexNullValue;
        private @Getter @Setter List<IndexConfig> indexConfigs;
    }

    public static class IndexConfig {
        private @Getter @Setter String name;
        private @Getter @Setter String map;
    }
    
}
