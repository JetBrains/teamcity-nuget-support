

package jetbrains.buildServer.nuget.server.trigger.impl.checker;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:51
 */
public class PackageCheckerNuGetPerPackage extends PackageCheckerNuGetBase implements PackageChecker {
  private static final Logger LOG = Logger.getInstance(PackageCheckerNuGetPerPackage.class.getName());

  private final ListPackagesCommand myCommand;
  private final PackageCheckerSettings mySettings;

  public PackageCheckerNuGetPerPackage(@NotNull final ListPackagesCommand command,
                                       @NotNull final PackageCheckerSettings settings) {
    myCommand = command;
    mySettings = settings;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    return super.accept(request) && !mySettings.allowBulkMode();
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<CheckablePackage> data) {
    final MultiMap<File, CheckablePackage> entries = new MultiMap<File, CheckablePackage>();
    for (CheckablePackage entry : data) {
      entries.putValue(getNuGetPath(entry.getMode()), entry);
    }

    for (Map.Entry<File, List<CheckablePackage>> nuget : entries.entrySet()) {
      final File nugetPath = nuget.getKey();
      for (final CheckablePackage packageCheckEntry : nuget.getValue()) {
        packageCheckEntry.setExecuting();
        final String packageId = packageCheckEntry.getPackage().getPackageId();
        executor.submit(ExceptionUtil.catchAll("Check update of NuGet package " + packageId, new Runnable() {
          public void run() {
            try {
              final SourcePackageReference pkg = packageCheckEntry.getPackage();
              Map<SourcePackageReference, ListPackagesResult> map = myCommand.checkForChanges(nugetPath, Collections.singleton(pkg));
              packageCheckEntry.setResult(CheckResult.fromResult(map.get(pkg)));
            } catch (Throwable t) {
              LOG.warnAndDebugDetails("Failed to check changes of " + packageId + ". " + t.getMessage(), t);
              packageCheckEntry.setResult(CheckResult.failed(t.getMessage()));
            }
          }
        }));
      }
    }
  }
}
