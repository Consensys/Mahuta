package net.consensys.tools.ipfs.ipfsstore.configuration;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AbstractConfiguration {

  protected boolean enable = true;
  protected String id;
  protected String host;
  protected Integer port;
  protected Map<String, String> additional;
  
}
