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

package jetbrains.buildServer.nuget.agent.parameters.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 17:59
 */
public class PackageSourceParser {
  private static final Logger LOG = Logger.getInstance(PackageSourceParser.class.getName());

  @NotNull
  public Collection<PackageSource> parseSources(@NotNull Collection<String> sources) {
    final List<PackageSource> list = new ArrayList<PackageSource>();
    for (String _source : sources) {
      if (StringUtil.isEmptyOrSpaces(_source)) continue;
      final String source = _source.trim();
      final String lowSource = source.toLowerCase();
      if (source.contains("@")  && (lowSource.startsWith("http://") || lowSource.startsWith("https://"))) {
        final Matcher matcher = Pattern.compile("^(https?://)(.*):([^@]*)@(.*)$", Pattern.CASE_INSENSITIVE).matcher(source);
        if (matcher.matches()) {
          try {
            final String url = (matcher.group(1) + matcher.group(4));
            final String userName = URLDecoder.decode(matcher.group(2), "utf-8");
            final String password = URLDecoder.decode(matcher.group(3), "utf-8");

            list.add(createSource(url, userName, password));
            continue;
          } catch (IOException e) {
            LOG.warn("Failed to parse NuGet Feed Source: " + _source);
          }
        }
      }

      list.add(createSource(source, null, null));
    }
    return list;
  }

  @NotNull
  private PackageSource createSource(@NotNull final String url,
                                     @Nullable final String userName,
                                     @Nullable final String password) {
    return new PackageSource() {
      @NotNull
      public String getSource() {
        return url;
      }

      public String getUserName() {
        return userName;
      }

      public String getPassword() {
        return password;
      }
    };
  }
}
