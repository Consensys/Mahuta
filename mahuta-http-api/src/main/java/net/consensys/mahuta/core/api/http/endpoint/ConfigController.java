package net.consensys.mahuta.core.api.http.endpoint;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.exception.TechnicalException;

@RestController
public class ConfigController {

    private final Mahuta mahuta;

    @Autowired
    public ConfigController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    /**
     * Create an index
     *
     * @param index Index name
     * @throws ServiceException
     */
    @PostMapping(value = "${mahuta.api-spec.config.index}")
    public void createIndexWithConfiguration(
            @PathVariable(value = "index") String indexName,
            @RequestBody String configuration) {
        try {
            mahuta.createIndex(indexName, IOUtils.toInputStream(configuration, "UTF-8"));
        } catch (IOException e) {
            throw new TechnicalException("Error while reading the configuration", e);
        }
    }
}
