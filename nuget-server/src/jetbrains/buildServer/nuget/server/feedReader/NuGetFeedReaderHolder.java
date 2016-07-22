/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feedReader;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReader;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReaderImpl;
import jetbrains.buildServer.nuget.feedReader.NuGetPackage;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedGetMethodFactory;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedUrlResolver;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetPackagesFeedParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Evgeniy.Koshkin on 11-Mar-16.
 */
public class NuGetFeedReaderHolder implements NuGetFeedReader {
  private final NuGetFeedReader myFeedReader;

  public NuGetFeedReaderHolder() {
    NuGetFeedGetMethodFactory getMethodFactory = new NuGetFeedGetMethodFactory();
    myFeedReader = new NuGetFeedReaderImpl(new NuGetFeedUrlResolver(getMethodFactory), getMethodFactory, new NuGetPackagesFeedParser());
  }

  @NotNull
  @Override
  public Collection<NuGetPackage> queryPackageVersions(@NotNull NuGetFeedClient nuGetFeedClient, @NotNull String feedUrl, @NotNull String packageId) throws IOException {
    return myFeedReader.queryPackageVersions(nuGetFeedClient, feedUrl, packageId);
  }

  @Override
  public void downloadPackage(@NotNull NuGetFeedClient nuGetFeedClient, @NotNull String downloadUrl, @NotNull File destination) throws IOException {
    myFeedReader.downloadPackage(nuGetFeedClient, downloadUrl, destination);
  }
}
