package net.consensys.mahuta.api.http.configuration;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import net.consensys.mahuta.api.http.configuration.MahutaSettings.IPFS;
import net.consensys.mahuta.api.http.configuration.MahutaSettings.IPFSCluster;
import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.pinning.ipfs.IPFSClusterPinningService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.utils.BytesUtils;

@Configuration
public class MahutaConfiguration {

    private final MahutaSettings settings;

    @Autowired
    public MahutaConfiguration(MahutaSettings settings) {
        this.settings = settings;
    }

    @Bean
    public Mahuta mahuta() {
        
        // Configure IPFS service
        IPFSService storageService = readSettings(settings.getIpfs());
        
        // Configure IPFS replicas
        settings.getIpfs().getReplicaIPFS().stream()
            .map(this::readSettings)
            .filter(Objects::nonNull)
            .forEach(storageService::addReplica);

        // Configure IPFS Cluster replicas
        settings.getIpfs().getReplicaIPFSCluster().stream()
            .map(this::readSettings)
            .filter(Objects::nonNull)
            .forEach(storageService::addReplica);

        // Configure ElasticSearch
        ElasticSearchService indexerService = ElasticSearchService.connect(settings.getElasticSearch().getHost(),
                settings.getElasticSearch().getPort(), settings.getElasticSearch().getClusterName());

        // Configure ElasticSearch index 
        settings.getElasticSearch().getIndexConfigs().stream()
                .filter(index -> !StringUtils.isEmpty(index.getName()))
                .forEach(index -> indexerService.withIndex(index.getName(), BytesUtils.readFileInputStream(index.getMap())));
        
        
        return new MahutaFactory()
                .configureStorage(storageService)
                .configureIndexer(indexerService)
                .defaultImplementation();
    }
    
    private IPFSService readSettings(IPFS ipfs) {
        
        if(StringUtils.isEmpty(ipfs.getHost()) && StringUtils.isEmpty(ipfs.getMultiaddress())) {
            return null;
        }
        
        return Optional.ofNullable(ipfs.getMultiaddress())
                .map(IPFSService::connect)
                .orElseGet(() -> IPFSService.connect(ipfs.getHost(), ipfs.getPort()))
                .configureThreadPool(ipfs.getThreadPool())
                .configureReadTimeout(ipfs.getTimeout().getRead())
                .configureWriteTimeout(ipfs.getTimeout().getWrite());
    }
    
    private IPFSClusterPinningService readSettings(IPFSCluster ipfsCluster) {
        
        if(StringUtils.isEmpty(ipfsCluster.getHost())) {
            return null;
        }
        
        return Optional.ofNullable(ipfsCluster.getProtocol())
                .map(protocol -> IPFSClusterPinningService.connect(
                        ipfsCluster.getHost(), 
                        ipfsCluster.getPort(), 
                        ipfsCluster.getProtocol()))
                .orElseGet(() -> IPFSClusterPinningService.connect(
                        ipfsCluster.getHost(), 
                        ipfsCluster.getPort()));
    }

}
