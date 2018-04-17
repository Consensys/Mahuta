package net.consensys.tools.ipfs.ipfsstore.dao.pinning;

import net.consensys.tools.ipfs.ipfsstore.configuration.AbstractConfiguration;
import net.consensys.tools.ipfs.ipfsstore.dao.PinningStrategy;
import net.consensys.tools.ipfs.ipfsstore.exception.DaoException;

public class InfuraPinningStrategy implements PinningStrategy {

  public static final String NAME = "infura";
  
  public InfuraPinningStrategy(AbstractConfiguration config) { }
  
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void pin(String hash) throws DaoException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unpin(String hash) throws DaoException {
    throw new UnsupportedOperationException();
  }

}
