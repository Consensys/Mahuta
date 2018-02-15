package net.consensys.tools.ipfs.ipfsstore.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A filter represent an operation on an index field based on a value
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Filter {

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("operation")
    private QueryOperation operation;
    
    @JsonProperty("value")
    private Object value;
    
}
