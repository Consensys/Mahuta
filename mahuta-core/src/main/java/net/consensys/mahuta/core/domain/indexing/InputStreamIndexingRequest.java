package net.consensys.mahuta.core.domain.indexing;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InputStreamIndexingRequest extends AbstractIndexingRequest{

    private InputStream content;

}
