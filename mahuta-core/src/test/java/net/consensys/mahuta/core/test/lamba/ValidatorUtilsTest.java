package net.consensys.mahuta.core.test.lamba;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import net.consensys.mahuta.core.utils.ValidatorUtils;

public class ValidatorUtilsTest {

    private static final String FIELD_NAME = "field";
    private static final String MESSAGE = "message";
    private static final String NULL_STRING = null;
    private static final Collection<?> NULL_COLLECTION = null;
    
    
    @Test
    public void isNotNull() {
        assertFalse(ValidatorUtils.isNull("hello"));
        assertTrue(ValidatorUtils.isNull(null));
    }
    
    @Test
    public void isEmptyString() {
        assertTrue(ValidatorUtils.isEmpty(NULL_STRING));
        assertTrue(ValidatorUtils.isEmpty(""));
        assertFalse(ValidatorUtils.isEmpty("hello"));
    }
    
    @Test
    public void isEmptyArray() {
        assertTrue(ValidatorUtils.isEmpty(NULL_COLLECTION));
        assertTrue(ValidatorUtils.isEmpty(Collections.emptyList()));
        assertTrue(ValidatorUtils.isEmpty());

        assertFalse(ValidatorUtils.isEmpty(Arrays.asList("hello")));
        assertFalse(ValidatorUtils.isEmpty("1", "2"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfNull() {
        ValidatorUtils.rejectIfNull(FIELD_NAME, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfNullWithMessage() {
        ValidatorUtils.rejectIfNull(FIELD_NAME, null, MESSAGE);
    }
    
    @Test
    public void rejectIfNullOK() {
        ValidatorUtils.rejectIfNull(FIELD_NAME, "hello");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfEmpty() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, "");
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfEmpty2() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME);
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfEmpty3() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, Arrays.asList());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfEmptyWithMessage() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, null, MESSAGE);
    }
    
    @Test
    public void rejectIfEmptyOK() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, "hello");
    }
    
    @Test
    public void rejectIfEmptyOK2() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, "1", "2");
    }
    
    @Test
    public void rejectIfEmptyOK3() {
        ValidatorUtils.rejectIfEmpty(FIELD_NAME, Arrays.asList("1"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfNegative() {
        ValidatorUtils.rejectIfNegative(FIELD_NAME, -10);
    }
    
    @Test
    public void rejectIfNegativeOK() {
        ValidatorUtils.rejectIfNegative(FIELD_NAME, 3);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void rejectIfDifferentThan() {
        ValidatorUtils.rejectIfDifferentThan(FIELD_NAME, "test", "VAL1", "VAL2");
    }
    
    @Test
    public void rejectIfDifferentThanOK() {
        ValidatorUtils.rejectIfDifferentThan(FIELD_NAME, "VAL2", "VAL1", "VAL2");
    }

    
}
