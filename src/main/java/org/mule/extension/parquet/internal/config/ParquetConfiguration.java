package org.mule.extension.parquet.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;

@Operations(ParquetOperations.class)
@ConnectionProviders(ParquetConnectionProvider.class)
public class ParquetConfiguration {

  @Parameter
  private String configId;

  public String getConfigId(){
    return configId;
  }
}
