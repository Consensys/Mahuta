package net.consensys.mahuta.core.domain.searching;

/**
 * Query operation allowed for filtering search in the index
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
public enum QueryOperation {

    FULL_TEXT, // Full text search
    EQUALS, // Equals
    NOT_EQUALS, // Not equals
    CONTAINS, // Contains the word/phrase
    IN, // in the following list
    GT, // Greater than
    GTE, // Greater than or Equals
    LT, // Less than
    LTE // Less than or Equals

}
