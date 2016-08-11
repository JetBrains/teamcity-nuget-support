/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.feed.server.cache;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
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
}
