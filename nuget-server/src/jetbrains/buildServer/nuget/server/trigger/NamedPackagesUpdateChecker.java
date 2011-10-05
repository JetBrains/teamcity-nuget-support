package jetbrains.buildServer.nuget.server.trigger;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:41
 */
public class NamedPackagesUpdateChecker implements TriggerUpdateChecker {
  private static final Logger LOG = Logger.getInstance(NamedPackagesUpdateChecker.class.getName());

  public static final String KEY = "hash";
  private final NuGetToolManager myManager;
  private final PackageChangesManager myPackageChangesManager;
  private final CheckRequestModeFactory myModeFactory;
  private final PackageCheckRequestFactory myRequestFactory;


  public NamedPackagesUpdateChecker(@NotNull final NuGetToolManager manager,
                                    @NotNull final PackageChangesManager packageChangesManager,
                                    @NotNull final CheckRequestModeFactory modeFactory,
                                    @NotNull final PackageCheckRequestFactory requestFactory) {
    myManager = manager;
    myPackageChangesManager = packageChangesManager;
    myModeFactory = modeFactory;
    myRequestFactory = requestFactory;
  }
  
  @NotNull 
  private Collection<String> parsePackageIds(@Nullable String ids) {
    if (ids == null || StringUtil.isEmptyOrSpaces(ids)) return Collections.emptyList();
    Set<String> packages = new HashSet<String>();
    for (String pId : ids.split("[\\r\\n]+")) {
      final String id = pId.trim();
      if (id.length() == 0) continue;
      packages.add(id);
    }
    return packages;
  }

  @Nullable
  public BuildStartReason checkChanges(@NotNull BuildTriggerDescriptor descriptor,
                                       @NotNull CustomDataStorage storage) throws BuildTriggerException {
    final String path = myManager.getNuGetPath(descriptor.getProperties().get(TriggerConstants.NUGET_EXE));
    final Collection<String> pkgIds = parsePackageIds(descriptor.getProperties().get(TriggerConstants.PACKAGE));
    final String version = descriptor.getProperties().get(TriggerConstants.VERSION);
    final String source = descriptor.getProperties().get(TriggerConstants.SOURCE);

    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new BuildTriggerException("Path to NuGet.exe must be specified");
    }

    if (pkgIds.isEmpty()) {
      throw new BuildTriggerException("Package Id must be specified");
    }

    final File nugetPath = new File(path);
    if (!nugetPath.isFile()) {
      throw new BuildTriggerException("Failed to find NuGet.exe at: " + nugetPath);
    }

    BuildStartReason reason = null;
    BuildTriggerException exception = null;

    for (String pkgId : pkgIds) {
      final PackageCheckRequest checkRequest = myRequestFactory.createRequest(
              myModeFactory.createNuGetChecker(nugetPath),
              source,
              pkgId,
              version
      );


      final CheckResult result = getResultAndPostTask(pkgId, checkRequest);
      if (result == null) continue;

      final String error = result.getError();
      if (error != null) {
        //noinspection ThrowableInstanceNeverThrown
        exception = new BuildTriggerException("Failed to check for package versions of " + pkgId + ". " + error, exception);
        continue;
      }

      final String key = KEY + "-" + pkgId;
      final String hash = serializeHashcode(result.getInfos());
      final String oldHash = storage.getValue(key);

      LOG.debug("Recieved packages hash: " + hash);
      LOG.debug("          old hash was: " + oldHash);
      if (!hash.equals(oldHash)) {
        storage.putValue(key, hash);
        storage.flush();

        if (oldHash != null) {
          //we need to update all triggers to avoid multiple triggering
          reason = new BuildStartReason(pkgId, reason);
        }
      }
    }

    //first if there were a change detected let the build start.
    if (reason != null) return reason;

    //if nothing detected, let it fail
    if (exception != null) throw exception;

    //ok. let's try again latter
    return null;
  }

  @Nullable
  private CheckResult getResultAndPostTask(@NotNull final String pkgId, @NotNull final PackageCheckRequest checkRequest) {
    try {
      return myPackageChangesManager.checkPackage(checkRequest);
      //no change available
    } catch (Throwable t) {
      final String msg = "Failed to ckeck changes for package: " + pkgId + ". " + t.getMessage();
      LOG.warn(msg, t);
      return CheckResult.failed(msg);
    }
  }

  private String serializeHashcode(@NotNull final Collection<SourcePackageInfo> _packages) {
    List<SourcePackageInfo> sorted = new ArrayList<SourcePackageInfo>(_packages);
    Collections.sort(sorted, new Comparator<SourcePackageInfo>() {
      public int compare(SourcePackageInfo o1, SourcePackageInfo o2) {
        int i;
        String s1 = o1.getSource();
        String s2 = o2.getSource();
        if (s1 == null && s2 == null) return 0;
        if (s1 == null && s2 != null) return 1;
        if (s1 != null && s2 == null) return -1;
        if (0 != (i = s1.compareTo(s2))) return i;
        if (0 != (i = o1.getPackageId().compareTo(o2.getPackageId()))) return i;
        if (0 != (i = o1.getVersion().compareTo(o2.getVersion()))) return i;
        return 0;
      }
    });

    StringBuilder sb = new StringBuilder();
    for (SourcePackageInfo info : sorted) {
      String source = info.getSource();
      if (source != null) {
        sb.append("|s:").append(source);
      }
      sb.append("|p:").append(info.getPackageId());
      sb.append("|v:").append(info.getVersion());
    }
    return sb.toString();
  }
}
