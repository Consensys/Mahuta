package net.consensys.mahuta.core.domain.common.pagination;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.ToString;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@Getter @ToString
public class Page<T> {

    private final PageRequest pageRequest;
    private final List<T> elements;
    private final Integer totalElements;
    private final Integer totalPages;
    
    private Page(PageRequest pageRequest, List<T> elements, Integer totalElements) {
        ValidatorUtils.rejectIfNull("pageRequest", pageRequest);
        ValidatorUtils.rejectIfNull("elements", elements);
        ValidatorUtils.rejectIfNegative("totalElements", totalElements);
        
        this.pageRequest = pageRequest;
        this.elements = elements;
        this.totalElements = totalElements;
        this.totalPages = (totalElements + pageRequest.getSize() - 1) / pageRequest.getSize();
    }
    
    public static <T> Page<T> of(PageRequest pageRequest, List<T> elements, Integer totalElements) {
        return new Page<>(pageRequest, elements, totalElements);
    }
    
    public static <T> Page<T> of(T element) {
        return new Page<>(PageRequest.singleElementPage(), Arrays.asList(element), 1);
    }
    
    public boolean isEmpty() {
        return totalElements == 0;
    }
    
}