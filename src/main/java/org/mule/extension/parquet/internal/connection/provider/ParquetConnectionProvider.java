package org.mule.extension.parquet.internal.connection.provider;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetConnectionProvider implements PoolingConnectionProvider<ParquetConnection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParquetConnectionProvider.class);


  @Override
  public ParquetConnection connect() throws ConnectionException {
    return new ParquetConnection(null);
  }

  @Override
  public void disconnect(ParquetConnection connection) {
    try {
      connection.invalidate();
    } catch (Exception e) {
      LOGGER.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(ParquetConnection connection) {
    return ConnectionValidationResult.success();
  }
}
