package org.mule.extension.parquet.internal;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetConnectionProvider implements PoolingConnectionProvider<ParquetConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(ParquetConnectionProvider.class);

 /**
  * A parameter that is always required to be configured.
  */
  @Parameter
  private String requiredParameter;

 /**
  * A parameter that is not required to be configured by the user.
  */
  @DisplayName("Friendly Name")
  @Parameter
  @Optional(defaultValue = "100")
  private int optionalParameter;

  @Override
  public ParquetConnection connect() throws ConnectionException {
    return new ParquetConnection(requiredParameter + ":" + optionalParameter);
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
