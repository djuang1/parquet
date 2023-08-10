package org.mule.extension.parquet.internal.config;

import org.mule.extension.parquet.internal.connection.provider.ParquetConnectionProvider;
import org.mule.extension.parquet.internal.operation.ParquetOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Operations(ParquetOperations.class)
@ConnectionProviders(ParquetConnectionProvider.class)
public class ParquetConfiguration {

}
