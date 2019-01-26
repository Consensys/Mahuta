/**
 * 
 */
package net.consensys.mahuta.core.domain;

import java.io.OutputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MetadataAndPayload {
    
    private @Getter Metadata metadata;
    private @Getter OutputStream payload;
    
    public static MetadataAndPayload of(Metadata metadata, OutputStream payload) {
        return new MetadataAndPayload(metadata, payload);
    }
}
