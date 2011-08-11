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

package jetbrains.buildServer.nuget.server.feed.reader;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 15:42
 */
public class NuGetFeedReader {
  private FeedClient myClient;

  public NuGetFeedReader(FeedClient client) {
    myClient = client;
  }

  public void queryPackage(@NotNull String feedUrl,
                           @NotNull String packageId) throws IOException {
    HttpGet get = new HttpGet(feedUrl + "/Packages()");
    get.getParams().setParameter("$filter", "Id eq '" + packageId + "'");
    get.setHeader(HttpHeaders.ACCEPT_ENCODING, "application/xml");
    final HttpResponse execute = myClient.getClient().execute(get);

    System.out.println(execute);
    execute.getEntity().writeTo(System.out);
  }
}
