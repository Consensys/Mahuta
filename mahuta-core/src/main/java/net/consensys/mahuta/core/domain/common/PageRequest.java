package net.consensys.mahuta.core.domain.common;

import lombok.Getter;
import lombok.ToString;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@ToString
public class PageRequest {

    public static final int DEFAULT_SIZE = 20;

    public enum SortDirection {
        ASC, DESC
    }

    private final @Getter Integer page;
    private final @Getter Integer size;
    private final @Getter String sort;
    private final @Getter SortDirection direction;

    private PageRequest(Integer page, Integer size, String sort, SortDirection direction) {
        ValidatorUtils.rejectIfNegative("page", page);
        ValidatorUtils.rejectIfNegative("size", size);
        
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
    }

    public static PageRequest of() {
        return new PageRequest(0, DEFAULT_SIZE, null, SortDirection.ASC);
    }

    public static PageRequest of(final int page, final int size) {
        return new PageRequest(page, size, null, SortDirection.ASC);
    }

    public static PageRequest of(final int page, final int size, final String sort, final SortDirection direction) {
        return new PageRequest(page, size, sort, direction);
    }
    
    public static PageRequest singleElementPage() {
        return PageRequest.of(0, 1);
    }
    
    public boolean isAscending() {
        return direction.equals(SortDirection.ASC);
    }
}
