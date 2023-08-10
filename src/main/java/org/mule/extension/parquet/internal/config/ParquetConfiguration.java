package org.mule.extension.parquet.internal.config;

import org.mule.extension.parquet.internal.connection.provider.ParquetConnectionProvider;
import org.mule.extension.parquet.internal.operation.ParquetOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Operations(ParquetOperations.class)
@ConnectionProviders(ParquetConnectionProvider.class)
public class ParquetConfiguration {

  @Parameter
  @Placement(order = 1,tab = "General")
  private String configId;

  public String getConfigId(){
    return configId;
  }
}
