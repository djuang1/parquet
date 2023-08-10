package org.mule.extension.parquet.internal.error;

import static java.util.Optional.ofNullable;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Optional;

public enum ParquetErrorType implements ErrorTypeDefinition<ParquetErrorType>{
    
    EXECUTION;
    
    private ErrorTypeDefinition parent;

    ParquetErrorType() {

    }

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return ofNullable(parent);
    }
}
