

package jetbrains.buildServer.nuget.server.tool.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.ZipSlipAwareZipFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageValidationUtil {
  private static final Logger LOG = Logger.getInstance(NuGetPackageValidationUtil.class.getName());

  private static final String PATH_TO_NUGET_EXE_IN_DISTRIB_PACKAGE = "tools/nuget.exe";
  private static final String PATH_TO_NUGET_EXE_IN_DISTRIB_PACKAGE_OLD = "tools/NuGet.exe";

  public static void validatePackage(@NotNull final File pkg) throws ToolException {
    ZipSlipAwareZipFile file = null;
    try {
      file = new ZipSlipAwareZipFile(pkg);
      if (file.getEntry(PATH_TO_NUGET_EXE_IN_DISTRIB_PACKAGE) == null && file.getEntry(PATH_TO_NUGET_EXE_IN_DISTRIB_PACKAGE_OLD) == null) {
        throw new ToolException("NuGet package must contain " + PATH_TO_NUGET_EXE_IN_DISTRIB_PACKAGE + " file");
      }
    } catch (IOException e) {
      String msg = "Failed to read NuGet package file. " + e.getMessage();
      LOG.warnAndDebugDetails(msg, e);
      throw new ToolException(msg);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          //NOP
        }
      }
    }
  }

}
