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

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:43
 */
public class PackageCheckerTeamCity implements PackageChecker {
  private final NuGetFeedReader myReader;

  public PackageCheckerTeamCity(@NotNull NuGetFeedReader reader) {
    myReader = reader;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    if (!(request.getMode() instanceof CheckRequestModeTeamCity)) return false;
    if (!StringUtil.isEmptyOrSpaces(request.getPackageSource())) return false;
    String uri = getUri(request);
    try {
      new URI(uri);
    } catch (Throwable t) {
      return false;
    }

    return true;
  }

  @NotNull
  private String getUri(@NotNull PackageCheckRequest request) {
    String uri = request.getPackageSource();
    if (uri == null) uri = FeedConstants.MS_REF_FEED;
    return uri;
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<PackageCheckEntry> entries) {
    for (final PackageCheckEntry entry : entries) {
      entry.setExecuting();
      executor.submit(new Runnable() {
        public void run() {
          final PackageCheckRequest req = entry.getRequest();
          try {
            final Collection<FeedPackage> packages = myReader.queryPackageVersions(getUri(req), req.getPackageId());
            final Collection<SourcePackageInfo> infos = new ArrayList<SourcePackageInfo>();
            for (FeedPackage aPackage : packages) {
              infos.add(new SourcePackageInfo(req.getPackageSource(), req.getPackageId(), aPackage.getInfo().getVersion()));
            }
            entry.setResult(CheckResult.succeeded(infos));
          } catch (Throwable e) {
            entry.setResult(CheckResult.failed(e.getMessage()));
          }
        }
      });
    }
  }
}
