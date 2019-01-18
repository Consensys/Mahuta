package net.consensys.mahuta.core.test.page;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.domain.common.Page;
import net.consensys.mahuta.core.domain.common.PageRequest;

@Slf4j
public class PageTest {

    @Test
    public void checkField() {
        Integer pageNo = 0;
        Integer size = 20;
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> content = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 50;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, content, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(size, page.getPageRequest().getSize());
        assertEquals(pageNo, page.getPageRequest().getPage());
        assertEquals(content.size(), page.getContent().size());
        assertEquals(totalElements, page.getTotalElements());
    }

    @Test
    public void calculateTotalPage1() {
        Integer pageNo = 0;
        Integer size = 20;
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> content = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 50;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, content, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(Integer.valueOf(3), page.getTotalPages());
    }

    @Test
    public void calculateTotalPage2() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> content = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 12;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, content, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(Integer.valueOf(1), page.getTotalPages());
    }

    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnPageRequest() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = null;
        List<Integer> content = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 12;

        /////////////////////////////
        Page.of(null, content, totalElements);
        /////////////////////////////
    }
    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnContent() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> content = null;
        Integer totalElements = -12;

        /////////////////////////////
        Page.of(pageRequest, content, totalElements);
        /////////////////////////////
    }
    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnTotalElements() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> content = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = -12;

        /////////////////////////////
        Page.of(pageRequest, content, totalElements);
        /////////////////////////////
    }

}
