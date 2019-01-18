package net.consensys.mahuta.core.domain.common;

import java.util.List;

import lombok.Getter;
import lombok.ToString;
import net.consensys.mahuta.core.utils.ValidatorUtils;

@ToString
public class Page<T> {

    private final @Getter PageRequest pageRequest;
    private final @Getter List<T> content;
    private final @Getter Integer totalElements;
    private final @Getter Integer totalPages;
    
    private Page(PageRequest pageRequest, List<T> content, Integer totalElements) {
        ValidatorUtils.rejectIfNull("pageRequest", pageRequest);
        ValidatorUtils.rejectIfNull("content", content);
        ValidatorUtils.rejectIfNegative("totalElements", totalElements);
        
        this.pageRequest = pageRequest;
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = (totalElements + pageRequest.getSize() - 1) / pageRequest.getSize();
    }
    
    public static <T> Page<T> of(PageRequest pageRequest, List<T> content, Integer totalElements) {
        return new Page<>(pageRequest, content, totalElements);
    }
    
    public boolean isEmpty() {
        return totalElements == 0;
    }
    
}
