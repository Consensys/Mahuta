package net.consensys.mahuta.client.springdata.utils;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;

import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest.SortDirection;

public class MahutaSpringDataUtils {

    private MahutaSpringDataUtils() {}
    
    public static PageRequest convertPageable(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                pageable.getSort().get().findFirst().map(Order::getProperty).orElseGet(() -> null), 
                pageable.getSort().get().findFirst().map(s->s.getDirection().isAscending() ? SortDirection.ASC : SortDirection.DESC).orElseGet(() -> null));
    }
    
}
