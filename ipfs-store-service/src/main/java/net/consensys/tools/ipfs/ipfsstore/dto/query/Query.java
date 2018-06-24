package net.consensys.tools.ipfs.ipfsstore.dto.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * "Query" accumulates filters and acts as a query builder for each allowed operation
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
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

    public static Query newQuery() {
        return new Query();
    }

    public static Query newQuery(List<Filter> filterClauses) {
        return new Query(filterClauses);
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

    public Query fullText(String name, String value) {
        this.filterClauses.add(new Filter(name, QueryOperation.full_text, value));
        return this;
    }

    public Query fullText(String[] names, String value) {
        this.filterClauses.add(new Filter(names, QueryOperation.full_text, value));
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

    public Query in(String name, Object... values) {
        this.filterClauses.add(new Filter(name, QueryOperation.in, values));
        return this;
    }

    public Query lessThan(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.lt, value));
        return this;
    }

    public Query greaterThan(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.gt, value));
        return this;
    }

    public Query lessThanOrEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.lte, value));
        return this;
    }

    public Query greaterThanOrEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.gte, value));
        return this;
    }

    @JsonIgnore
    public boolean isEmpty() {
        if (this == null || this.getFilterClauses().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
