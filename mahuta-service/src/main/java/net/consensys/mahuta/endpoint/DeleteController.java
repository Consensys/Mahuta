package net.consensys.mahuta.endpoint;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.consensys.mahuta.exception.NotFoundException;
import net.consensys.mahuta.service.StoreService;

@RestController
public class DeleteController {

    private final StoreService storeService;

    @Autowired
    public DeleteController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Delete content
     *
     * @param id
     * @param index
     * @throws NotFoundException 
     */
    @DeleteMapping("${mahuta.api-spec.persistence.delete.id}")
    public void deleteById(            
    		@PathVariable(value = "id") @NotNull String id,
            @RequestParam(value = "index", required = true) String index) throws NotFoundException {

        this.storeService.removeFileById(index, id);
    }

    /**
     * Delete content
     *
     * @param hash
     * @param index
     * @throws NotFoundException 
     */
    @DeleteMapping("${mahuta.api-spec.persistence.delete.hash}")
    public void deleteByHash(            
    		@PathVariable(value = "hash") @NotNull String hash,
            @RequestParam(value = "index", required = true) String index) throws NotFoundException {

        this.storeService.removeFileByHash(index, hash);
    }

}
