package net.consensys.mahuta.core.api.http.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.MahutaFactory;
import net.consensys.mahuta.core.indexer.elasticsearch.ElasticSearchService;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.consensys.mahuta.core.utils.FileUtils;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Configuration
public class MahutaConfiguration {

    private final MahutaSettings settings;

    @Autowired
    public MahutaConfiguration(MahutaSettings settings) {
        this.settings = settings;
    }

    @Bean
    public Mahuta mahuta() {
        
        IPFSService storageService = Optional.ofNullable(settings.getIpfs().getMultiaddress())
                .map((multiaddress) -> IPFSService.connect(multiaddress))
                .orElseGet(() -> IPFSService.connect(settings.getIpfs().getHost(), settings.getIpfs().getPort()))
                .configureThreadPool(settings.getIpfs().getThreadPool())
                .configureTimeout(settings.getIpfs().getTimeout());

        ElasticSearchService indexerService = ElasticSearchService.connect(settings.getElasticSearch().getHost(),
                settings.getElasticSearch().getPort(), settings.getElasticSearch().getClusterName());

        if (!ValidatorUtils.isEmpty(settings.getElasticSearch().getIndexConfigs())) {
            settings.getElasticSearch().getIndexConfigs()
                    .forEach(config -> indexerService.withIndex(config.getName(), FileUtils.readFileInputString(config.getMap())));
        }
        
        return new MahutaFactory()
                .configureStorage(storageService)
                .configureIndexer(indexerService)
                .build();
    }

}
