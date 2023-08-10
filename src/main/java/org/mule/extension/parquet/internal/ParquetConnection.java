package org.mule.extension.parquet.internal;

public final class ParquetConnection {

  private final String id;

  public ParquetConnection(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void invalidate() {
    // do something to invalidate this connection!
  }
}
