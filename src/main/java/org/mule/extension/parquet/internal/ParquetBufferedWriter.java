package org.mule.extension.parquet.internal;

import org.apache.parquet.io.DelegatingPositionOutputStream;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ParquetBufferedWriter implements OutputFile {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
    @Override
    public PositionOutputStream create(long blockSizeHint) throws IOException {
        return new InMemoryPositionOutputStream(baos);
    }
 
    @Override
    public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
        return null;
    }
 
    @Override
    public boolean supportsBlockSize() {
        return false;
    }
 
    @Override
    public long defaultBlockSize() {
        return 0;
    }
 
    public byte[] toArray() {
        return baos.toByteArray();
    }
 
    private static class InMemoryPositionOutputStream extends DelegatingPositionOutputStream {
 
        public InMemoryPositionOutputStream(OutputStream outputStream) {
            super(outputStream);
        }
 
        @Override
        public long getPos() throws IOException {
            return ((ByteArrayOutputStream) this.getStream()).size();
        }
    }
}