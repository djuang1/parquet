package org.mule.extension.parquet.internal.io;

// https://github.com/tideworks/arvo2parquet

import org.apache.parquet.io.PositionOutputStream;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public final class OutputFile {
  private static final int IO_BUF_SIZE = 16 * 1024;

  public static org.apache.parquet.io.OutputFile nioPathToOutputFile(@Nonnull final Path file) {

    return new org.apache.parquet.io.OutputFile() {
      @Override
      public PositionOutputStream create(long blockSizeHint) throws IOException {
        return makePositionOutputStream(file, IO_BUF_SIZE, false);
      }

      @Override
      public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
        return makePositionOutputStream(file, IO_BUF_SIZE, true);
      }

      @Override
      public boolean supportsBlockSize() {
        return false;
      }

      @Override
      public long defaultBlockSize() {
        return 0;
      }
    };
  }

  private static PositionOutputStream makePositionOutputStream(@Nonnull Path file, int ioBufSize, boolean trunc)
          throws IOException
  {
    //final OutputStream output = new BufferedOutputStream(
    //        Files.newOutputStream(file, CREATE, trunc ? TRUNCATE_EXISTING : APPEND), ioBufSize);

    OutputStream output = Files.newOutputStream(file, CREATE, trunc ? TRUNCATE_EXISTING : APPEND);
    BufferedOutputStream bufferedOutput = new BufferedOutputStream(output, ioBufSize);
        
    return new PositionOutputStream() {
      private long position = 0;

      @Override
      public void write(int b) throws IOException {
        bufferedOutput.write(b);
        position++;
      }

      @Override
      public void write(@Nonnull byte[] b) throws IOException {
        bufferedOutput.write(b);
        position += b.length;
      }

      @Override
      public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        bufferedOutput.write(b, off, len);
        position += len;
      }

      @Override
      public void flush() throws IOException {
        bufferedOutput.flush();
      }

      @Override
      public void close() throws IOException {
        try {
          bufferedOutput.close();
        } finally {
          output.close();
        }
      }

      @Override
      public long getPos() throws IOException {
        return position;
      }
    };
  }
}