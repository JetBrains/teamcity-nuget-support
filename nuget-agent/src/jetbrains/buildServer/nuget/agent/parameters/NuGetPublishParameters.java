

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 15:56
 */
public interface NuGetPublishParameters extends NuGetParameters {
  @Nullable
  String getPublishSource() throws RunBuildException;

  @Nullable
  String getApiKey() throws RunBuildException;

  @NotNull
  Collection<String> getFiles() throws RunBuildException;

  @NotNull
  Collection<String> getCustomCommandline();
}
