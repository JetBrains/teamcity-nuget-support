

package jetbrains.buildServer.nuget.feed.server.cache;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.04.13 9:42
 */
public class ResponseOutputStream extends ServletOutputStream {
  private final OutputStream myStream;
  private final AtomicBoolean myClosed = new AtomicBoolean(false);

  public ResponseOutputStream(@NotNull final OutputStream stream) throws IOException {
    myStream = new GZIPOutputStream(stream);
  }

  @Override
  public void write(int b) throws IOException {
    myStream.write(b);
  }

  @Override
  public void write(@NotNull byte[] b, int off, int len) throws IOException {
    myStream.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    myStream.flush();
  }

  @Override
  public void close() throws IOException {
    if (!myClosed.compareAndSet(false, true)) return;
    myStream.close();
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public void setWriteListener(WriteListener listener) {
  }
}
