package net.consensys.mahuta.core.indexer.elasticsearch;

import lombok.Getter;
import lombok.Setter;

public class ElasticSearchSettings {
    
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 9300;
    public static final boolean DEFAULT_INDEX_NULL_VALUES = true;
    public static final boolean DEFAULT_ENABLE_OPENTRACING = false;

    private @Setter @Getter String host = DEFAULT_HOST;
    private @Setter @Getter Integer port = DEFAULT_PORT;
    private @Setter @Getter String clusterName;
    private @Setter @Getter boolean indexNullValue = DEFAULT_INDEX_NULL_VALUES;
    private @Setter @Getter boolean enableOpenTracing = DEFAULT_ENABLE_OPENTRACING;
    
    public static ElasticSearchSettings of(String host, Integer port, String clusterName) {
        ElasticSearchSettings s = new ElasticSearchSettings();
        s.setHost(host);
        s.setPort(port);
        s.setClusterName(clusterName);
        return s;
    }
}
