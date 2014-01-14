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

package jetbrains.buildServer.nuget.tests.integration.http;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
* @author Pavel.Sher
*         Date: 05.05.2009
*/
public class SimpleHttpServer extends SimpleHttpServerBase {

  private Response myResponse = new Response(STATUS_LINE_200, Collections.<String>emptyList()) {
    @Override
    public void printContent(final PrintStream ps) throws IOException {
    }

    @Override
    public Integer getLength() {
      return 0;
    }
  };

  public void setResponse(@NotNull String statusLine, @NotNull List<String> headers, @NotNull final String content) {
    myResponse = createStringResponse(statusLine, headers, content);
  }

  public void setResponse(@NotNull String statusLine, @NotNull List<String> headers, @NotNull final byte[] content) {
    myResponse = createStreamResponse(statusLine, headers, content);
  }

  public void setResponse(@NotNull String statusLine, @NotNull List<String> headers, @NotNull final InputStream content) {
    myResponse = createStreamResponse(statusLine, headers, content);
  }

  @Override
  protected Response getResponse(final String request) throws IOException {
    return getSimpleHttpServerResponse(request);
  }


  protected final Response getSimpleHttpServerResponse(final String request) throws IOException {
    return myResponse;
  }

}
