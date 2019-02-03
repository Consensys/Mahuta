package net.consensys.mahuta.core.domain.createindex;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.core.domain.Request;

@Getter @Setter @ToString
public class CreateIndexRequest implements Request {
    
    private String name;
    private InputStream configuration;

}
