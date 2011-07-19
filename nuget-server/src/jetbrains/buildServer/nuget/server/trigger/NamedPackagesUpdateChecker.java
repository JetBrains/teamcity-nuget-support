package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:41
 */
public class NamedPackagesUpdateChecker implements TriggerUpdateChecker {
  public static final String KEY = "hash";
  private final ListPackagesCommand myCommand;

  public NamedPackagesUpdateChecker(@NotNull final ListPackagesCommand command) {
    myCommand = command;
  }

  public BuildStartReason checkChanges(@NotNull BuildTriggerDescriptor descriptor,
                                       @NotNull CustomDataStorage storage) throws BuildTriggerException {
    final String path = descriptor.getProperties().get(TriggerConstants.NUGET_EXE);
    final String pkgId = descriptor.getProperties().get(TriggerConstants.PACKAGE);
    final String version = descriptor.getProperties().get(TriggerConstants.VERSION);
    final String source = descriptor.getProperties().get(TriggerConstants.SOURCE);

    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new BuildTriggerException("Path to NuGet.exe must be specified");
    }

    if (StringUtil.isEmptyOrSpaces(pkgId)) {
      throw new BuildTriggerException("Package Id must be specified");
    }

    File nugetPath = new File(path);
    if (!nugetPath.isFile()) {
      throw new BuildTriggerException("Failed to find NuGet.exe at: " + nugetPath);
    }

    Collection<SourcePackageInfo> result;
    try {
      result = myCommand.checkForChanges(nugetPath, source, pkgId, version);
    } catch (Throwable t) {
      throw new BuildTriggerException("Failed to check for package versions. " + t.getMessage(), t);
    }
    final String hash = serializeHashcode(result);

    String oldHash = storage.getValue(KEY);
    if (oldHash != null && !hash.equals(oldHash)) {
      storage.putValue(KEY, hash);
      storage.flush();
      return new BuildStartReason("NuGet Package " + pkgId + " updated");
    }

    return null;
  }

  private String serializeHashcode(@NotNull final Collection<SourcePackageInfo> _packages) {
    List<SourcePackageInfo> sorted = new ArrayList<SourcePackageInfo>(_packages);
    Collections.sort(sorted, new Comparator<SourcePackageInfo>() {
      public int compare(SourcePackageInfo o1, SourcePackageInfo o2) {
        int i;
        if (0 != (i = o1.getSource().compareTo(o2.getSource()))) return i;
        if (0 != (i = o1.getPackageId().compareTo(o2.getPackageId()))) return i;
        if (0 != (i = o1.getVersion().compareTo(o2.getVersion()))) return i;
        return 0;
      }
    });

    StringBuilder sb = new StringBuilder();
    for (SourcePackageInfo info : sorted) {
      sb.append("|s:").append(info.getSource());
      sb.append("|p:").append(info.getPackageId());
      sb.append("|v:").append(info.getVersion());
    }
    return sb.toString();
  }
}
