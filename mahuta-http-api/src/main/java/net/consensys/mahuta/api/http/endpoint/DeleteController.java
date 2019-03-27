package net.consensys.mahuta.api.http.endpoint;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import net.consensys.mahuta.core.Mahuta;
import net.consensys.mahuta.core.domain.deindexing.DeindexingResponse;
import net.consensys.mahuta.core.exception.NoIndexException;
import net.consensys.mahuta.core.exception.NotFoundException;

@RestController
public class DeleteController {

    private final Mahuta mahuta;

    @Autowired
    public DeleteController(Mahuta mahuta) {
        this.mahuta = mahuta;
    }

    /**
     * Delete content
     *
     * @param id
     * @param index
     * @throws NotFoundException
     */
    @DeleteMapping("${mahuta.api-spec.v1.persistence.delete.id}")
    public DeindexingResponse deleteById(
            @PathVariable(value = "id") @NotNull String id,
            @RequestParam(value = "index", required = true) String indexName) {

        try {
            return mahuta.prepareDeindexing(indexName, id).execute();
        } catch (NoIndexException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

}
