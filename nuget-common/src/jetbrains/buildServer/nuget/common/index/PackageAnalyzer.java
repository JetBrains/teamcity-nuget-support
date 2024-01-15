

package jetbrains.buildServer.nuget.common.index;

import jetbrains.buildServer.nuget.common.PackageLoadException;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

/**
 * Analyzes package contents.
 */
public interface PackageAnalyzer {
    String SHA512 = "SHA512";

    @NotNull
    Map<String, String> analyzePackage(@NotNull InputStream content) throws PackageLoadException;

    @NotNull
    String getSha512Hash(@NotNull InputStream content) throws PackageLoadException;
}
