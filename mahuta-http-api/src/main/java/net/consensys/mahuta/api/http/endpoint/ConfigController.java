package net.consensys.mahuta.api.http.endpoint;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.exception.TechnicalException;
import net.consensys.mahuta.core.utils.lamba.Throwing;

/**
 * HTTP/REST Controler responsible of config setting:
 * - Indexes management
 * 
 * @author gjeanmart
 *
 */
@RestController
public class ConfigController {

    private final Mahuta mahuta;

    @Autowired
    public ConfigController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    /**
     * Get indexes list
     * 
     * @return List of Indexes
     */
    @GetMapping(value = "${mahuta.api-spec.v1.config.index.list}")
    public List<String> getIndexes() {
        
        return mahuta.getIndexes();
    }

    /**
     * Create an index
     *
     * @param index         Index name
     * @param configuration Optional configuration to associate with the index (mapping file)
     */
    @PostMapping(value = "${mahuta.api-spec.v1.config.index.create}")
    public void createIndex(
            @PathVariable(value = "index") String indexName,
            @RequestBody(required=false) Optional<String> configuration) {

        mahuta.createIndex(indexName, configuration.map(Throwing.rethrowFunc(c -> IOUtils.toInputStream(c, "UTF-8"))).orElse(null));
    }
    
}
