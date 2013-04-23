/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed.cache;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.04.13 9:44
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
  private final ByteArrayOutputStream myCache = new ByteArrayOutputStream(65536);
  private final Map<String, String> myHeaders = new HashMap<String, String>();
  private final ResponseOutputStream myOutput;
  private final PrintWriter myWriter;
  private int myStatus = SC_OK;

  public ResponseWrapper(@NotNull final HttpServletResponse response) throws IOException {
    super(response);
    myOutput = new ResponseOutputStream(myCache);
    myWriter = new PrintWriter(new OutputStreamWriter(myOutput, "utf-8"), false);
  }

  @Override
  public void setHeader(String name, String value) {
    super.setHeader(name, value);
    myHeaders.put(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    super.addHeader(name, value);
    myHeaders.put(name, value);
  }

  @Override
  public void setStatus(int sc) {
    super.setStatus(sc);
    myStatus = sc;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return myOutput;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return myWriter;
  }

  public void closeAll() throws IOException {
    myWriter.close();
    myOutput.close();
  }

  @NotNull
  public ResponseCacheEntry build() throws IOException {
    closeAll();
    return new ResponseCacheEntry(myHeaders, myCache.toByteArray(), myStatus);
  }
}

