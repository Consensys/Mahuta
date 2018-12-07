package net.consensys.mahuta.client.java.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * RestResponsePage is a JSON serializable version of org.springframework.data.domain.PageImpl<T>
 *
 * @param <T> Type of object
 * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T extends Serializable> extends PageImpl<T> {

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
    @JsonIgnore
    private Pageable pageable;

    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    public RestResponsePage() {
        super(new ArrayList<>());
    }

    public PageImpl<T> pageImpl() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(),
                getSize(), getSort()), getTotalElements());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RestResponsePage<?> that = (RestResponsePage<?>) o;
        return number == that.number &&
                size == that.size &&
                totalPages == that.totalPages &&
                numberOfElements == that.numberOfElements &&
                totalElements == that.totalElements &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), number, size, totalPages, numberOfElements, totalElements, content);
    }
}