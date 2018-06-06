package net.consensys.tools.ipfs.ipfsstore.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.consensys.tools.ipfs.ipfsstore.service.StoreService;

@RestController
public class ConfigController {

    private final StoreService storeService;

    @Autowired
    public ConfigController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Create an index
     *
     * @param index Index name
     * @throws ServiceException
     */
    @RequestMapping(value = "${ipfs-store.api-spec.config.index}", method = RequestMethod.POST)
    public void createIndex(@PathVariable(value = "index") String index) {

        this.storeService.createIndex(index);
    }
}
