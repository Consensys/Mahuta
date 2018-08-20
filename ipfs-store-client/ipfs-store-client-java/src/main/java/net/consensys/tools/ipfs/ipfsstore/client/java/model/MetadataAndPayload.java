/**
 * 
 */
package net.consensys.tools.ipfs.ipfsstore.client.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;

@AllArgsConstructor
@Builder
public class MetadataAndPayload {
    private @Getter @Setter Metadata metadata;
    private @Getter @Setter byte[] payload;
}
