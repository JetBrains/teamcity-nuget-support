

package jetbrains.buildServer.nuget.common;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.spec.NuspecFileContent;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.ZipSlipAwareZipInputStream;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.zip.ZipEntry;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 16:34
 */
public class PackageInfoLoader {

  private static final Logger LOG = Logger.getInstance(PackageInfoLoader.class.getName());

  @NotNull
  public NuGetPackageInfo loadPackageInfo(@NotNull final File pkg) throws PackageLoadException {

    final Element root = parseNuSpec(pkg);
    if (root == null) {
      throw new PackageLoadException("Failed to fetch .nuspec from package");
    }

    final NuspecFileContent nuspec = new NuspecFileContent(root);

    final String id = nuspec.getId();
    final String version = nuspec.getVersion();

    if (id == null || StringUtil.isEmptyOrSpaces(id)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Id for package: " + pkg.getPath());
    }

    if (version == null || StringUtil.isEmptyOrSpaces(version)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Version for package: " + pkg.getPath());
    }

    return new NuGetPackageInfo(id, version);
  }

  @Nullable
  private static Element parseNuSpec(@NotNull final File nupkg) throws PackageLoadException {
    ZipSlipAwareZipInputStream zos = null;
    InputStream stream = null;
    try {
      stream = new FileInputStream(nupkg);
      zos = new ZipSlipAwareZipInputStream(new BufferedInputStream(stream));
      ZipEntry ze;
      while ((ze = zos.getNextEntry()) != null) {
        if (ze.getName().endsWith(FeedConstants.NUSPEC_FILE_EXTENSION)) {
          try {
            return FileUtil.parseDocument(zos, false);
          } catch (JDOMException e) {
            LOG.warn("Failed to parse " + ze + " in " + nupkg);
          }
        }
      }
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to read " + nupkg + ". " + e.getMessage(), e);
    } finally {
      close(zos);
      FileUtil.close(stream);
    }

    return null;
  }

  private static void close(@Nullable final ZipSlipAwareZipInputStream zos) {
    if (zos != null) {
      try {
        zos.close();
      } catch (IOException e) {
        //NOP
      }
    }
  }
}
