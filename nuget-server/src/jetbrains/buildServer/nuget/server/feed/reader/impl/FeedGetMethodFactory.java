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

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 12:28
 */
public class FeedGetMethodFactory {
  @NotNull
  public HttpGet createGet(@NotNull final String url, NameValuePair... getParams) {
    String argz = URLEncodedUtils.format(Arrays.asList(getParams), HTTP.ISO_8859_1);
    if (argz.length() > 0) {
      argz = (url.contains("?") ? "&" : "?") + argz;
    }
    final HttpGet get = new HttpGet(url + argz);
    HttpProtocolParams.setVersion(get.getParams(), HttpVersion.HTTP_1_1);

    return get;
  }
}
