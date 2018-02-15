package net.consensys.tools.ipfs.ipfsstore.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * "Query" accumulates filters and acts as a query builder for each allowed operation
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Query {

    @JsonProperty("query")
    private final List<Filter> filterClauses;
    
    public Query() {
        this.filterClauses = new ArrayList<>();
    }
    public Query(List<Filter> filterClauses) {
        this.filterClauses = filterClauses;
    }
    
    public Query filter(Filter filter) {
        this.filterClauses.add(filter);
        return this;
    }
    
    public Query filter(String name, QueryOperation operation, Object value) {
        this.filterClauses.add(new Filter(name, operation, value));
        return this;
    }
    
    public Query equals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.equals, value));
        return this; 
    }
    
    public Query notEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.not_equals, value));
        return this; 
    }
    
    public Query contains(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.contains, value));
        return this; 
    }
    
    public Query in(String name, Object[] value) {
        this.filterClauses.add(new Filter(name, QueryOperation.in, value));
        return this; 
    }
    
    public Query lessThan(String name, Object[] value) {
        this.filterClauses.add(new Filter(name, QueryOperation.lt, value));
        return this; 
    }
    
    public Query greaterThan(String name, Object[] value) {
        this.filterClauses.add(new Filter(name, QueryOperation.gt, value));
        return this; 
    }
    
    public Query lessThanOrEquals(String name, Object[] value) {
        this.filterClauses.add(new Filter(name, QueryOperation.lte, value));
        return this; 
    }
    
    public Query greaterThanOrEquals(String name, Object[] value) {
        this.filterClauses.add(new Filter(name, QueryOperation.gte, value));
        return this; 
    }
}
