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
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 15:23
 */
public class CheckRequestModeFactory {
  private final NuGetFeedReader myReader;
  private final ListPackagesCommand myCommand;

  public CheckRequestModeFactory(@NotNull NuGetFeedReader reader,
                                 @NotNull ListPackagesCommand command) {
    myReader = reader;
    myCommand = command;
  }

  public CheckRequestMode createNuGetChecker(@NotNull final File nugetPath) {
    return new CheckRequestMode() {
      public void checkForUpdates(@NotNull ScheduledExecutorService executor, @NotNull Collection<PackageCheckEntry> value) {
        for (final PackageCheckEntry packageCheckEntry : value) {
          //TODO: join into one request
          executor.execute(new Runnable() {
            public void run() {
              final PackageCheckRequest req = packageCheckEntry.getRequest();
              try {
                final Collection<SourcePackageInfo> infos = myCommand.checkForChanges(nugetPath, req.getPackageSource(), req.getPackageId(), req.getVersionSpec());
                packageCheckEntry.setResult(CheckResult.succeeded(infos));
              } catch (Throwable t) {
                packageCheckEntry.setResult(CheckResult.failed(t.getMessage()));
              }
            }
          });
        }
      }
    };
  }

  @NotNull
  public CheckRequestMode craeteJavaChecker() {
    return new CheckRequestMode() {
      public void checkForUpdates(@NotNull ScheduledExecutorService executor, @NotNull Collection<PackageCheckEntry> value) {
        for (final PackageCheckEntry entry : value) {
          //TODO: update API to create a batch request to nuget feed
          executor.submit(ExceptionUtil.catchAll("check NuGet package version: " + entry.getRequest().getPackageId(), new Runnable() {
            public void run() {
              final PackageCheckRequest req = entry.getRequest();
              String feedUrl = req.getPackageSource();
              if (feedUrl == null || StringUtil.isEmptyOrSpaces(feedUrl)) {
                feedUrl = FeedConstants.MS_REF_FEED;
              }
              try {
                final Collection<FeedPackage> packages = myReader.queryPackageVersions(feedUrl, req.getPackageId());
                final Collection<SourcePackageInfo> infos = new ArrayList<SourcePackageInfo>();
                for (FeedPackage aPackage : packages) {
                  infos.add(new SourcePackageInfo(req.getPackageSource(), req.getPackageId(), aPackage.getInfo().getVersion()));
                }
                entry.setResult(CheckResult.succeeded(infos));
              } catch (Throwable e) {
                entry.setResult(CheckResult.failed(e.getMessage()));
              }
            }
          }));
        }
      }
    };
  }

}
