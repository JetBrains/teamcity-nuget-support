

package jetbrains.buildServer.nuget.common.index;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.spec.NuspecFileContent;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.ZipSlipAwareZipInputStream;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageStructureVisitor {

  private static final Logger LOG = Logger.getInstance(NuGetPackageStructureVisitor.class.getName());

  @NotNull private final Collection<NuGetPackageStructureAnalyser> myAnalysers;

  public NuGetPackageStructureVisitor(@NotNull Collection<NuGetPackageStructureAnalyser> analysers) {
    myAnalysers = analysers;
  }

  public void visit(@NotNull InputStream stream) {
    if(myAnalysers.isEmpty()) return;
    ZipSlipAwareZipInputStream zipInputStream = null;
    try {
      zipInputStream = new ZipSlipAwareZipInputStream(new BufferedInputStream(stream));
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if(zipEntry.isDirectory()) continue;
        final String zipEntryName = zipEntry.getName();
        for(NuGetPackageStructureAnalyser analyser : myAnalysers){
          analyser.analyseEntry(zipEntryName);
        }
        if (zipEntryName.endsWith(FeedConstants.NUSPEC_FILE_EXTENSION)) {
          LOG.debug(String.format("Nuspec file found on path %s in NuGet package", zipEntryName));
          final NuspecFileContent nuspecContent = readNuspecFileContent(zipInputStream);
          if (nuspecContent == null)
            LOG.warn("Failed to read .nuspec file content from NuGet package");
          else {
            for(NuGetPackageStructureAnalyser analyser : myAnalysers){
              analyser.analyseNuspecFile(nuspecContent);
            }
          }
          zipInputStream.closeEntry();
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to read content of NuGet package");
      FileUtil.close(zipInputStream);
    }
  }

  @Nullable
  private NuspecFileContent readNuspecFileContent(final ZipSlipAwareZipInputStream finalZipInputStream) throws IOException {
    try {
      final Element document = FileUtil.parseDocument(new InputStream() {
        @Override
        public int read() throws IOException {
          return finalZipInputStream.read();
        }

        @Override
        public void close() {
          //do nothing, should avoid stream closing by xml parse util
        }
      }, false);
      return new NuspecFileContent(document);
    } catch (JDOMException e) {
      LOG.debug(e);
      return null;
    }
  }
}
