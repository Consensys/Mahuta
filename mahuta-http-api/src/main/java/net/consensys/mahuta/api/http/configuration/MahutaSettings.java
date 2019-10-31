package net.consensys.mahuta.api.http.configuration;

import java.util.ArrayList;
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

    private static final int IPFS_PORT = 5001;
    private static final int IPFSCLUSTER_PORT = 8084;
    private static final String IPFSCLUSTER_PROTOCOL = "http";
    private static final int IPFS_TIMEOUT_MS = 5000;
    private static final int IPFS_THREAD_POOL = 10;
    private static final int ELATIC_PORT = 9200;
    
    private @Setter @Getter IPFS ipfs;
    private @Setter @Getter ElasticSearch elasticSearch;
    public static class IPFS {
        private @Getter @Setter String host;
        private @Getter @Setter Integer port = IPFS_PORT;
        private @Getter @Setter String multiaddress;
        private @Getter @Setter Integer timeout = IPFS_TIMEOUT_MS;
        private @Getter @Setter int threadPool = IPFS_THREAD_POOL;
        private @Getter @Setter List<IPFS> replicaIPFS = new ArrayList<>();
        private @Getter @Setter List<IPFSCluster> replicaIPFSCluster = new ArrayList<>();
    }

    public static class ElasticSearch {
        private @Getter @Setter String host;
        private @Getter @Setter Integer port = ELATIC_PORT;
        private @Getter @Setter String clusterName;
        private @Getter @Setter boolean indexNullValue;
        private @Getter @Setter List<IndexConfig> indexConfigs = new ArrayList<>();
    }

    public static class IndexConfig {
        private @Getter @Setter String name;
        private @Getter @Setter String map;
    }
    
    public static class IPFSCluster {
        private @Getter @Setter String host;
        private @Getter @Setter Integer port = IPFSCLUSTER_PORT;
        private @Getter @Setter String protocol = IPFSCLUSTER_PROTOCOL;
    }
    
}
