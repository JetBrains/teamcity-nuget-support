/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Serializable http response.
 */
public class SerializableHttpServletResponseWrapper extends HttpServletResponseWrapper {

  private StringWriter sw = new StringWriter();

  public SerializableHttpServletResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(sw);
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        sw.write(b);
      }
    };
  }

  public String toString() {
    return sw.toString();
  }
}
