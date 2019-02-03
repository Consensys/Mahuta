package net.consensys.mahuta.core.domain.common.pagination;

import lombok.Getter;
import lombok.ToString;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Getter @ToString
public class PageRequest {

    public static final int DEFAULT_SIZE = 20;

    public enum SortDirection {
        ASC, DESC
    }

    private final Integer page;
    private final Integer size;
    private final String sort;
    private final SortDirection direction;

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
