/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:28
 */
public class HttpAuthServer extends SimpleThreadedHttpServer {
  protected Response getAuthorizedResponse(String request) throws IOException {
    return getSimpleHttpServerResponse(request);
  }

  protected boolean authorizeUser(@NotNull final String loginPassword) {
    return true;
  }

  protected Response getResponse(String request) throws IOException {
    System.out.println(request);

    final String auth = getHeaderLine(request, "Authorization:");
    if (auth != null) {
      if (auth.startsWith("Basic")) {
        final String encoded = auth.substring("Basic".length()).trim();
        final String up = new String(new Base64().decode(encoded.getBytes("utf-8")), "utf-8");
        System.out.println("Login with: " + up);

        if (authorizeUser(up)) {
          return getAuthorizedResponse(request);
        }
      }
    }

    return new Response(STATUS_LINE_401, Arrays.asList("WWW-Authenticate: Basic realm=\"Secure Area\"", "Content-Type: text/plain")) {
      @Override
      public void printContent(PrintStream ps) throws IOException {
        ps.append("Authenticatino is required");
      }

      @Override
      public Integer getLength() {
        return null;
      }
    };
  }
}
