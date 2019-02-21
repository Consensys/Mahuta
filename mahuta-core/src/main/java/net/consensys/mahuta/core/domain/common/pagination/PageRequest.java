package net.consensys.mahuta.core.domain.common.pagination;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Getter @ToString
@NoArgsConstructor
public class PageRequest {
    public static final int FIRST_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public enum SortDirection {
        ASC, DESC
    }

    private Integer page;
    private Integer size;
    private String sort;
    private SortDirection direction;

    private PageRequest(Integer page, Integer size, String sort, SortDirection direction) {
        ValidatorUtils.rejectIfNegative("page", page);
        ValidatorUtils.rejectIfNegative("size", size);
        
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
    }

    public static PageRequest of() {
        return new PageRequest(FIRST_PAGE, DEFAULT_SIZE, null, SortDirection.ASC);
    }

    public static PageRequest of(final int page, final int size) {
        return new PageRequest(page, size, null, SortDirection.ASC);
    }

    public static PageRequest of(final int page, final int size, final String sort, final SortDirection direction) {
        return new PageRequest(page, size, sort, direction);
    }
    
    public static PageRequest singleElementPage() {
        return PageRequest.of(FIRST_PAGE, 1);
    }
    
    @JsonIgnore
    public boolean isAscending() {
        return direction.equals(SortDirection.ASC);
    }
}
