package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class InputStreamIndexingRequest extends AbstractIndexingRequest{

    private InputStream content;

}
