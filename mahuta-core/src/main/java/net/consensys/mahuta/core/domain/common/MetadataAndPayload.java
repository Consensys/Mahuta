/**
 * 
 */
package net.consensys.mahuta.core.domain.common;

import java.io.OutputStream;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MetadataAndPayload {
    
    protected  Metadata metadata;
    protected OutputStream payload;

}
