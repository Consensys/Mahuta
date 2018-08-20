/**
 * 
 */
package net.consensys.tools.ipfs.ipfsstore.client.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IdAndHash {
    private @Getter @Setter  String id;
    private @Getter @Setter String hash;
 
}
