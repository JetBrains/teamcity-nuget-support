

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USED_PACKAGES_DIR;
import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USED_PACKAGES_FILE;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:02
 */
public class PackagesInfoUploader {
  private final ArtifactsWatcher myPublisher;
  private final PackageDependenciesStore myStore;

  public PackagesInfoUploader(@NotNull final ArtifactsWatcher publisher,
                              @NotNull final PackageDependenciesStore store) {
    myPublisher = publisher;
    myStore = store;
  }

  public void uploadDepectedPackages(@NotNull final AgentRunningBuild build,
                                     @NotNull final PackageDependencies infos) throws IOException {
    File tmp = FileUtil.createTempDirectory("nuget", "packages", build.getBuildTempDirectory());
    File content = new File(tmp, NUGET_USED_PACKAGES_FILE);
    myStore.save(infos, content);
    myPublisher.addNewArtifactsPath(content.getPath() + " => " + NUGET_USED_PACKAGES_DIR);
  }
}
