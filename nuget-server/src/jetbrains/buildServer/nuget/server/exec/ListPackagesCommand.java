

package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 19:04
 */
public interface ListPackagesCommand {

  @NotNull
  Map<SourcePackageReference, ListPackagesResult> checkForChanges(@NotNull File nugetPath,
                                                                  @NotNull Collection<SourcePackageReference> refs) throws NuGetExecutionException;


}
