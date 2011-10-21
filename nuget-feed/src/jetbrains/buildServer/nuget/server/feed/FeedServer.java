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

package jetbrains.buildServer.nuget.server.feed;

import jetbrains.buildServer.nuget.server.feed.render.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 21:09
 */
public class FeedServer {
  private final NuGetContext myContext;
  private final FeedMetadataRenderer myMetadata;
  private final NuGetFeedRenderer myRootRenderer;
  private final NuGetPackagesFeedRenderer myPackagesFeedRenderer;

  public FeedServer(@NotNull final NuGetContext context,
                    @NotNull final NuGetFeedRenderer rootRenderer,
                    @NotNull final FeedMetadataRenderer metadata,
                    @NotNull final NuGetPackagesFeedRenderer packagesFeedRenderer) {
    myContext = context;
    myRootRenderer = rootRenderer;
    myMetadata = metadata;
    myPackagesFeedRenderer = packagesFeedRenderer;
  }

  public void handleRequest(@NotNull final String relPath,
                            @NotNull final Map<String, String> arguments,
                            @NotNull final Writer out) throws IOException {
    if (StringUtil.isEmptyOrSpaces(relPath) || "/".equals(relPath)) {
      try {
        myRootRenderer.renderFeed(myContext, out);
        return;
      } catch (final XMLStreamException e) {
        throw new IOException(e.getMessage()) {{initCause(e);}};
      }
    }

    if (relPath.equals("/$metadata")) {
      myMetadata.renderFeed(myContext, out);
      return;
    }

    if (relPath.equals("/Packages()")) {
      try {
        myPackagesFeedRenderer.renderFeed(myContext, Collections.<NuGetItem>emptyList(), out);
        return;
      } catch (final XMLStreamException e) {
        throw new IOException(e.getMessage()) {{initCause(e);}};
      }
    }

    throw new IOException("Unknown handler");
  }
}
