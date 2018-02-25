package net.consensys.tools.ipfs.ipfsstore.client.java.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * RestResponsePage is a JSON serializable version of org.springframework.data.domain.PageImpl<T>
 * 
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 *
 * @param <T> Type of object 
 */
@Getter
@Setter
public class RestResponsePage<T> extends PageImpl<T> {

    private static final long serialVersionUID = 1L;
    
    private int number;
    private int size;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;
    private List<T> content;

    @JsonIgnore
    private boolean previousPage;
    @JsonIgnore
    private boolean first;
    @JsonIgnore
    private boolean nextPage;
    @JsonIgnore
    private boolean last;
    @JsonIgnore
    private Sort sort;

    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    public RestResponsePage() {
        super(new ArrayList<T>());
    }

    public PageImpl<T> pageImpl() {
        return new PageImpl<T>(getContent(), new PageRequest(getNumber(),
                getSize(), getSort()), getTotalElements());
    }
}