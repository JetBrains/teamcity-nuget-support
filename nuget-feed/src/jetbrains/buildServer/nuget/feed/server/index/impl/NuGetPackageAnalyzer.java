package jetbrains.buildServer.nuget.feed.server.index.impl;

import com.google.common.collect.Lists;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.index.PackageAnalyzer;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Perform analysis of NuGet packages.
 */
public class NuGetPackageAnalyzer implements PackageAnalyzer {

    private static final String SHA512 = "SHA512";

    @NotNull
    @Override
    public Map<String, String> analyzePackage(@NotNull final InputStream content) throws PackageLoadException {
        final LocalNuGetPackageItemsFactory packageItemsFactory = new LocalNuGetPackageItemsFactory();
        final FrameworkConstraintsCalculator frameworkConstraintsCalculator = new FrameworkConstraintsCalculator();
        final List<NuGetPackageStructureAnalyser> analysers = Lists.newArrayList(frameworkConstraintsCalculator, packageItemsFactory);

        new NuGetPackageStructureVisitor(analysers).visit(content);

        final Map<String, String> metadata = packageItemsFactory.getItems();
        metadata.put(PACKAGE_HASH, sha512(content));
        metadata.put(PACKAGE_HASH_ALGORITHM, SHA512);
        metadata.put(TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(frameworkConstraintsCalculator.getPackageConstraints()));
        metadata.put(LAST_UPDATED, ODataDataFormat.formatDate(new Date()));

        return metadata;
    }

    @NotNull
    private static String sha512(@NotNull final InputStream content) throws PackageLoadException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(content);
            final byte[] hash = DigestUtils.sha512(is);
            //Buggy commons.codes added unnecessary newlines
            return Base64.encodeBase64String(hash).replaceAll("[\r\n]+", "");
        } catch (IOException e) {
            throw new PackageLoadException("Failed to compute SHA-512 for package");
        } finally {
            FileUtil.close(is);
        }
    }
}
