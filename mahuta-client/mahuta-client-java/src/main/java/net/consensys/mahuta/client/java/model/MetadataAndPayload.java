/**
 * 
 */
package net.consensys.mahuta.client.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.consensys.mahuta.dto.Metadata;

@AllArgsConstructor
@Builder
public class MetadataAndPayload {
    private @Getter @Setter Metadata metadata;
    private @Getter @Setter byte[] payload;
}
