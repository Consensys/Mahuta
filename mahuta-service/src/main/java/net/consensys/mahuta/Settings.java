package net.consensys.mahuta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class Settings {

    // ### CONSTANT ########################
    public static final String INDEXER_ELASTICSEARCH = "ELASTICSEARCH";
    public static final String STORAGE_IPFS = "IPFS";
    public static final String STORAGE_SWARM = "SWARM";
    public static final String ERROR_NOT_NULL_OR_EMPTY = "cannot be null or empty";
    // ########################################

    // ### HEATH CHECK ########################
    @Value("${mahuta.healthcheck.pollInterval:60000}")
    private Integer healthCheckPollInterval;
    // ########################################

    // ### SECURITY ########################
    @Value("${mahuta.security.cors.origins:*}")
    private String corsOrigins;
    @Value("${mahuta.security.cors.methods:GET,POST,PUT,OPTIONS,DELETE,PATCH}")
    private String corsMethods;
    @Value("${mahuta.security.cors.headers:Access-Control-Allow-Headers,Origin,X-Requested-With,Content-Type,Accept}")
    private String corsHeaders;
    @Value("${mahuta.security.cors.credentials:false}")
    private boolean corsCredentials;
    // ########################################

}
