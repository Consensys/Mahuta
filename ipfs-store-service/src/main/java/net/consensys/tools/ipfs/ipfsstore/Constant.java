package net.consensys.tools.ipfs.ipfsstore;

public abstract class Constant {

  public static final String INDEXER_ELASTICSEARCH = "ELASTICSEARCH";
  public static final String STORAGE_IPFS = "IPFS";
  public static final String STORAGE_SWARM = "SWARM";
  
  public static final String ERROR_NOT_NULL_OR_EMPTY = "cannot be null or empty";

  public static String printHash(String hash) {
      return "[hash=" + hash + "]";
  }
}
