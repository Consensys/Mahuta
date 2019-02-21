package net.consensys.mahuta.core.test.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest;
import net.consensys.mahuta.core.domain.common.pagination.PageRequest.SortDirection;

@Slf4j
public class PageRequestTest {

    @Test
    public void checkField1() {
        Integer pageNo = 0;
        Integer size = 20;

        /////////////////////////////
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        log.debug("pageRequest: {}", pageRequest);
        /////////////////////////////

        assertEquals(size, pageRequest.getSize());
        assertEquals(pageNo, pageRequest.getPage());
        assertNull(pageRequest.getSort());
        assertEquals(SortDirection.ASC, pageRequest.getDirection());
    }

    @Test
    public void checkField2() {
        Integer pageNo = 0;
        Integer size = 20;
        String sort = "id";
        SortDirection dir = SortDirection.DESC;

        /////////////////////////////
        PageRequest pageRequest = PageRequest.of(pageNo, size, sort, dir);
        log.debug("pageRequest: {}", pageRequest);
        /////////////////////////////

        assertEquals(size, pageRequest.getSize());
        assertEquals(pageNo, pageRequest.getPage());
        assertEquals(sort, pageRequest.getSort());
        assertEquals(dir, pageRequest.getDirection());
        assertFalse(pageRequest.isAscending());

    }

    @Test
    public void checkField3() {
        /////////////////////////////
        PageRequest pageRequest = PageRequest.of();
        log.debug("pageRequest: {}", pageRequest);
        /////////////////////////////

        assertEquals(Integer.valueOf(0), pageRequest.getPage());
        assertEquals(Integer.valueOf(20), pageRequest.getSize());
        assertNull(pageRequest.getSort());
        assertEquals(SortDirection.ASC, pageRequest.getDirection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnSize() {
        Integer pageNo = 0;
        Integer size = -20;

        ////////////////////////////
        PageRequest.of(pageNo, size);
        /////////////////////////////
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIllegalArgumentExceptionOnPageNo() {
        Integer pageNo = -1;
        Integer size = 20;

        /////////////////////////////
        PageRequest.of(pageNo, size);
        /////////////////////////////
    }

}
