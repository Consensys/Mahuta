package net.consensys.mahuta.core.test.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.consensys.mahuta.core.domain.searching.Query;
import net.consensys.mahuta.core.domain.searching.QueryOperation;

public class QueryTest {

    private static final String ATTRIBUTE_NAME1 = "name1";
    private static final String ATTRIBUTE_NAME2 = "name2";
    private static final String ATTRIBUTE_VALUE1 = "value1";
    private static final String ATTRIBUTE_VALUE2 = "value2";
    private static final List<String> ATTRIBUTE_NAMES = Arrays.asList(ATTRIBUTE_NAME1, ATTRIBUTE_NAME2);
    private static final List<String> ATTRIBUTE_VALUES = Arrays.asList(ATTRIBUTE_VALUE1, ATTRIBUTE_VALUE2);

    @Test
    public void build() {
        /////////////////////////////////////////
        Query query = Query.newQuery().fullText(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertNotNull(query);
    }
    
    @Test
    public void fulltext1() {
        /////////////////////////////////////////
        Query query = Query.newQuery().fullText(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.FULL_TEXT, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void fulltext2() {
        /////////////////////////////////////////
        Query query = Query.newQuery().fullText(ATTRIBUTE_NAMES.toArray(new String[0]), ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAMES.get(0), query.getFilterClauses().get(0).getNames()[0]);
        assertEquals(ATTRIBUTE_NAMES.get(1), query.getFilterClauses().get(0).getNames()[1]);
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.FULL_TEXT, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void equals() {
        /////////////////////////////////////////
        Query query = Query.newQuery().equals(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.EQUALS, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void notequals() {
        /////////////////////////////////////////
        Query query = Query.newQuery().notEquals(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.NOT_EQUALS, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void contains() {
        /////////////////////////////////////////
        Query query = Query.newQuery().contains(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.CONTAINS, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void in() {
        /////////////////////////////////////////
        Query query = Query.newQuery().in(ATTRIBUTE_NAME1, Arrays.asList(ATTRIBUTE_VALUES));
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        //assertEquals(ATTRIBUTE_VALUES, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.IN, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void lessThan() {
        /////////////////////////////////////////
        Query query = Query.newQuery().lessThan(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.LT, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void lessThanOrEquals() {
        /////////////////////////////////////////
        Query query = Query.newQuery().lessThanOrEquals(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.LTE, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void greaterThan() {
        /////////////////////////////////////////
        Query query = Query.newQuery().greaterThan(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.GT, query.getFilterClauses().get(0).getOperation());
    }
    
    @Test
    public void greaterThanOrEquals() {
        /////////////////////////////////////////
        Query query = Query.newQuery().greaterThanOrEquals(ATTRIBUTE_NAME1, ATTRIBUTE_VALUE1);
        /////////////////////////////////////////
        
        assertEquals(ATTRIBUTE_NAME1, query.getFilterClauses().get(0).getName());
        assertEquals(ATTRIBUTE_VALUE1, query.getFilterClauses().get(0).getValue());
        assertEquals(QueryOperation.GTE, query.getFilterClauses().get(0).getOperation());
    }
    
    
}
