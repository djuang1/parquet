package org.mule.extension.parquet.internal;

import org.apache.parquet.io.DelegatingSeekableInputStream;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import org.apache.commons.io.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ParquetBufferedReader implements InputFile {
    private final String streamId;
    private final byte[] data;

    private static class SeekableByteArrayInputStream extends ByteArrayInputStream {
        public SeekableByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public int getPos() {
            return this.pos;
        }
    }

    public ParquetBufferedReader(String streamId, InputStream stream) throws IOException {
        this.streamId = streamId;
        this.data = IOUtils.toByteArray(stream);
    }

    @Override
    public long getLength() throws IOException {
        return this.data.length;
    }

    @Override
    public SeekableInputStream newStream() throws IOException {
        return new DelegatingSeekableInputStream(new SeekableByteArrayInputStream(this.data)) {
            @Override
            public void seek(long newPos) throws IOException {
                ((SeekableByteArrayInputStream) this.getStream()).setPos((int) newPos);
            }

            @Override
            public long getPos() throws IOException {
                return ((SeekableByteArrayInputStream) this.getStream()).getPos();
            }
        };
    }

    @Override
    public String toString() {
        return "ParquetStream[" + streamId + "]";
    }
}
