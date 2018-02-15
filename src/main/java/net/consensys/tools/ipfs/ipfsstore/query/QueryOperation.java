package net.consensys.tools.ipfs.ipfsstore.query;

/**
 * Query operation allowed for filtering search in the index
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 */
public enum QueryOperation {

    equals,         // Equals
    not_equals,     // Not equals
    contains,       // Contains the word/phrase
    in,             // in the following list
    gt,             // Greater than
    gte,            // Greater than or Equals
    lt,             // Less than 
    lte;            // Less than or Equals
    
}
