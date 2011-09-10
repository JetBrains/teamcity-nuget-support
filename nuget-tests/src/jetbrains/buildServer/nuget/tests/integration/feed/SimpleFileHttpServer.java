/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed;

import java.io.File;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

/**
* Author: Nikita.Skvortsov
* Date: 1/17/11
*/
public class SimpleFileHttpServer extends SimpleHttpServer {
  protected File myRoot;

  public SimpleFileHttpServer(@NotNull File serverRoot) {
    myRoot = serverRoot;
  }

  @Override
  protected Response getResponse(final String request) {
    final String path = getRequestPath(request);
    if (path != null) {
      File file = new File(myRoot, path);
      return getFileResponse(file, Collections.<String>emptyList());
    } else {
      return createStringResponse(STATUS_LINE_500, Collections.<String>emptyList(), "");
    }
  }
}
