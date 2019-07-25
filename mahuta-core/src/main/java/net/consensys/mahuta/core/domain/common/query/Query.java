package net.consensys.mahuta.core.domain.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

/**
 * "Query" accumulates filters and acts as a query builder for each allowed operation
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@ToString
public class Query {

    @JsonProperty("query")
    private final @Getter List<Filter> filterClauses;

    @JsonProperty("or")
    private final @Getter List<Query> subFilterClauses;


    public static Query newQuery() {
        return new Query();
    }

    public static Query newQuery(List<Filter> filterClauses) {
        return new Query(filterClauses);
    }
    
    private Query() {
        this(new ArrayList<>(), new ArrayList<>());
    }
    
    private Query(List<Filter> filterClauses) {
        this(filterClauses, new ArrayList<>());
    }

    private Query(List<Filter> filterClauses, List<Query> subFilterClauses) {
        this.filterClauses = filterClauses;
        this.subFilterClauses = subFilterClauses;
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
        this.filterClauses.add(new Filter(name, QueryOperation.FULL_TEXT, value));
        return this;
    }

    public Query fullText(String[] names, String value) {
        this.filterClauses.add(new Filter(names, QueryOperation.FULL_TEXT, value));
        return this;
    }

    public Query equals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.EQUALS, value));
        return this;
    }

    public Query notEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.NOT_EQUALS, value));
        return this;
    }

    public Query contains(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.CONTAINS, value));
        return this;
    }

    public Query in(String name, Collection<?> values) {
        this.filterClauses.add(new Filter(name, QueryOperation.IN, values));
        return this;
    }

    public Query notIn(String name, Collection<?> values) {
        this.filterClauses.add(new Filter(name, QueryOperation.NOT_IN, values));
        return this;
    }

    public Query lessThan(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.LT, value));
        return this;
    }

    public Query greaterThan(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.GT, value));
        return this;
    }

    public Query lessThanOrEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.LTE, value));
        return this;
    }

    public Query greaterThanOrEquals(String name, Object value) {
        this.filterClauses.add(new Filter(name, QueryOperation.GTE, value));
        return this;
    }

    public Query or(Query query) {
        this.subFilterClauses.add(query);
        return this;
    }

    public boolean isEmpty() {
        return this.getFilterClauses().isEmpty() && this.getSubFilterClauses().isEmpty();
    }
}
