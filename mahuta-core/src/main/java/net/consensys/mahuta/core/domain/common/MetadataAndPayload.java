/**
 * 
 */
package net.consensys.mahuta.core.domain.common;

import java.io.OutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class MetadataAndPayload {
    
    protected  Metadata metadata;
    protected OutputStream payload;

}
