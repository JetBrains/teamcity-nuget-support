

package jetbrains.buildServer.nuget.common.index;

import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.common.version.FrameworkConstraints;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.LAST_UPDATED;

/**
 * Perform analysis of NuGet packages.
 */
public class NuGetPackageAnalyzer implements PackageAnalyzer {

    @NotNull
    @Override
    public Map<String, String> analyzePackage(@NotNull final InputStream content) throws PackageLoadException {
        final LocalNuGetPackageItemsFactory packageItemsFactory = new LocalNuGetPackageItemsFactory();
        final FrameworkConstraintsCalculator frameworkConstraintsCalculator = new FrameworkConstraintsCalculator();
        final List<NuGetPackageStructureAnalyser> analysers = Arrays.asList(frameworkConstraintsCalculator, packageItemsFactory);

        final InputStream inputStream = new BufferedInputStream(content);
        try {
            new NuGetPackageStructureVisitor(analysers).visit(inputStream);
        } finally {
            FileUtil.close(inputStream);
        }

        final Map<String, String> metadata = packageItemsFactory.getItems();
        String constraints = FrameworkConstraints.convertToString(frameworkConstraintsCalculator.getPackageConstraints());
        metadata.put(PackageConstants.TEAMCITY_FRAMEWORK_CONSTRAINTS, constraints);
        metadata.put(LAST_UPDATED, ODataDataFormat.formatDate(new Date()));

        return metadata;
    }

    @NotNull
    @Override
    public String getSha512Hash(@NotNull InputStream content) throws PackageLoadException {
        final InputStream inputStream = new BufferedInputStream(content);
        try {
            final byte[] hash = DigestUtils.sha512(inputStream);
            //Buggy commons.codes added unnecessary newlines
            return Base64.encodeBase64String(hash).replaceAll("[\r\n]+", "");
        } catch (IOException e) {
            throw new PackageLoadException("Failed to compute SHA-512 for package");
        } finally {
            FileUtil.close(inputStream);
        }
    }
}
