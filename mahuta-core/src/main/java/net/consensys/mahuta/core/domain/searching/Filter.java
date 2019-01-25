package net.consensys.mahuta.core.domain.searching;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A filter represent an operation on an index field based on a value
 *
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@NoArgsConstructor
@ToString
public class Filter {

    @Getter
    private String name;

    @Setter
    private String[] names;

    @Getter
    private QueryOperation operation;

    @Getter
    private Object value;

    public Filter(String name, QueryOperation operation, Object value) {
        this.name = name;
        this.operation = operation;
        this.value = value;
    }

    public Filter(String[] names, QueryOperation operation, Object value) {
        this.names = names;
        this.operation = operation;
        this.value = value;
    }

    /**
     * Get Index field names If the attribute 'names' is null, we return 'name'
     */
    public String[] getNames() {
        if (names == null || names.length == 0) {
            return new String[] { name };
        } else {
            return names;
        }
    }
}
