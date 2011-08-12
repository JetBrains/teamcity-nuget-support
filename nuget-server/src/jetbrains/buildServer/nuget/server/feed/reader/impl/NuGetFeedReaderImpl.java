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

package jetbrains.buildServer.nuget.server.feed.reader.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.nuget.server.feed.reader.*;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 15:42
 */
public class NuGetFeedReaderImpl implements NuGetFeedReader {
  private static final Logger LOG = Logger.getInstance(NuGetFeedReader.class.getName());
  private final FeedClient myClient;
  private final UrlResolver myResolver;
  private final FeedGetMethodFactory myMethodFactory;
  private final PackagesFeedParser myParser;

  public NuGetFeedReaderImpl(@NotNull final FeedClient client,
                             @NotNull final UrlResolver resolver,
                             @NotNull final FeedGetMethodFactory methodFactory,
                             @NotNull final PackagesFeedParser parser) {
    myClient = client;
    myResolver = resolver;
    myMethodFactory = methodFactory;
    myParser = parser;
  }

  @NotNull
  public Collection<FeedPackage> queryPackageVersions(@NotNull String feedUrl,
                                                      @NotNull String packageId) throws IOException {
    LOG.debug("Connecting to NuGet feed url: " + feedUrl);
    final Pair<String, HttpResponse> pair = myResolver.resolvePath(feedUrl);
    feedUrl = pair.first;
    LOG.debug("Resolved NuGet feed URL to " + feedUrl);
    final Element element = toDocument(pair.second);
    LOG.debug("Recieved xml: " + XmlUtil.to_s(element));

    final HttpGet get = myMethodFactory.createGet(feedUrl + "/Packages()",
            new Param("$filter", "Id eq '" + packageId + "'")
    );
    get.setHeader(HttpHeaders.ACCEPT_ENCODING, "application/atom+xml");

    LOG.debug("Query for packages: " + get.getURI());

    final HttpResponse execute = myClient.getClient().execute(get);
    return myParser.readPackages(toDocument(execute));
  }

  public void downloadPackage(@NotNull FeedPackage pkg, @NotNull File file) throws IOException {
    FileUtil.createParentDirs(file);
    final String url = pkg.getDownloadUrl();

    final HttpGet get = myMethodFactory.createGet(url);
    final HttpResponse resp = myClient.getClient().execute(get);
    final StatusLine statusLine = resp.getStatusLine();
    if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
      throw new IOException("Failed to download package " + pkg + ". Server returned " + statusLine);
    }

    OutputStream os = null;
    try {
      os = new BufferedOutputStream(new FileOutputStream(file));
      resp.getEntity().writeTo(os);
    } catch (final IOException e) {
      throw new IOException("Failed to download package " + pkg + ". " + e.getMessage()) {{ initCause(e); }};
    } finally {
      FileUtil.close(os);
    }
  }

  private Element toDocument(HttpResponse response) throws IOException {
    try {
      return FileUtil.parseDocument(response.getEntity().getContent(), false);
    } catch (final JDOMException e) {
      throw new IOException("Failed to parse xml document. " + e.getMessage()) {{
        initCause(e);
      }};
    }
  }
}
