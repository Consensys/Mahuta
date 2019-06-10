package net.consensys.mahuta.core.test.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.domain.common.pagination.Page;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;

@Slf4j
public class PageTest {

    @Test
    public void checkField() {
        Integer pageNo = 0;
        Integer size = 20;
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 50;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, elements, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(size, page.getPageRequest().getSize());
        assertEquals(pageNo, page.getPageRequest().getPage());
        assertEquals(elements.size(), page.getElements().size());
        assertEquals(totalElements, page.getTotalElements());
    }

    @Test
    public void calculateTotalPage1() {
        Integer pageNo = 0;
        Integer size = 20;
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 50;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, elements, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(Integer.valueOf(3), page.getTotalPages());
    }

    @Test
    public void calculateTotalPage2() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 12;

        /////////////////////////////
        Page<Integer> page = Page.of(pageRequest, elements, totalElements);
        log.debug("page: {}", page);
        /////////////////////////////

        assertEquals(Integer.valueOf(1), page.getTotalPages());
    }

    @Test
    public void getNextPageRequest() {
        Integer pageNo = 0;
        Integer size = 10;

        PageRequest pageRequest1 = PageRequest.of(pageNo, size);
        List<Integer> elements1 = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> elements2 = IntStream.range(0, 2).boxed().collect(Collectors.toList());
        Integer totalElements = 12;
        
        /////////////////////////////
        Page<Integer> page1 = Page.of(pageRequest1, elements1, totalElements);
        /////////////////////////////
        assertEquals(Integer.valueOf(0), pageRequest1.getPage());
        assertFalse(page1.isLast());
        assertTrue(page1.hasNext());


        /////////////////////////////
        PageRequest pageRequest2 = page1.nextPageRequest();
        Page<Integer> page2 = Page.of(pageRequest2, elements2, totalElements);
        /////////////////////////////
        assertEquals(Integer.valueOf(1), pageRequest2.getPage());
        assertTrue(page2.isLast());
        assertFalse(page2.hasNext());
    }

    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnPageRequest() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = null;
        List<Integer> elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = 12;

        /////////////////////////////
        Page.of(null, elements, totalElements);
        /////////////////////////////
    }
    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnContent() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> elements = null;
        Integer totalElements = -12;

        /////////////////////////////
        Page.of(pageRequest, elements, totalElements);
        /////////////////////////////
    }
    @Test(expected=IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnTotalElements() {
        Integer pageNo = 0;
        Integer size = 20;

        PageRequest pageRequest = PageRequest.of(pageNo, size);
        List<Integer> elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Integer totalElements = -12;

        /////////////////////////////
        Page.of(pageRequest, elements, totalElements);
        /////////////////////////////
    }

}
