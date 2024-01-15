

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 11:17
 */
public interface NuGetPackParameters extends NuGetParameters {

  @NotNull
  Collection<String> getSpecFiles() throws RunBuildException;

  @NotNull
  Collection<String> getExclude();

  @NotNull
  Collection<String> getProperties();

  @NotNull
  Collection<String> getCustomCommandline();

  @NotNull
  File getOutputDirectory() throws RunBuildException;

  boolean cleanOutputDirectory() throws RunBuildException;

  @NotNull
  PackagesPackDirectoryMode getBaseDirectoryMode();

  @NotNull
  File getBaseDirectory() throws RunBuildException;

  @Nullable
  String getVersion() throws RunBuildException;

  boolean packSymbols();
  boolean packTool();

  boolean publishAsArtifacts();

  boolean preferProjectFileToNuSpec();
}
