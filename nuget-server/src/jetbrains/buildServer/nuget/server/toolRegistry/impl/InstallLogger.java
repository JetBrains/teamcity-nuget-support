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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 16.08.11 0:09
*/
public interface InstallLogger {
  void started(@NotNull String packageId);

  void packageNotFound(@NotNull String packageId);

  void packageDownloadStarted(@NotNull FeedPackage tool);

  void packageDownloadFinished(@NotNull FeedPackage tool, @Nullable File pkg);

  void packageDownloadFailed(@NotNull FeedPackage tool, @Nullable File pkg, @NotNull Exception e);

  void packageUnpackStarted(@NotNull FeedPackage tool, @NotNull File pkg);

  void packageUnpackFailed(@NotNull FeedPackage tool, @NotNull File pkg, @Nullable File dest);

  void packageUnpackFinished(@NotNull FeedPackage tool, @NotNull File pkg, @Nullable File dest);

  void agentToolPackStarted(@NotNull FeedPackage tool, @NotNull File dest);

  void agentToolPackFailed(@NotNull FeedPackage tool, @NotNull File dest, @NotNull Exception e);

  void agentToolPackFinished(@NotNull FeedPackage tool);

  void agentToolPubslishStarted(@NotNull FeedPackage tool, @NotNull File agentTool);

  void agentToolPublishFailed(@NotNull FeedPackage tool, @NotNull File agentTool, @NotNull Exception e);

  void agentToolPuglishFinished(@NotNull FeedPackage tool, @NotNull File agentTool);

  void finished(@NotNull String packageId, @Nullable FeedPackage tool);
}
