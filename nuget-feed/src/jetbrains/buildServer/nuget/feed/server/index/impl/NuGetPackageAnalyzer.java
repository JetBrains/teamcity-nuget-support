/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.feed.server.index.impl;

import com.google.common.collect.Lists;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.index.PackageAnalyzer;
import jetbrains.buildServer.nuget.common.version.FrameworkConstraints;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Perform analysis of NuGet packages.
 */
public class NuGetPackageAnalyzer implements PackageAnalyzer {

    @NotNull
    @Override
    public Map<String, String> analyzePackage(@NotNull final InputStream content) throws PackageLoadException {
        final LocalNuGetPackageItemsFactory packageItemsFactory = new LocalNuGetPackageItemsFactory();
        final FrameworkConstraintsCalculator frameworkConstraintsCalculator = new FrameworkConstraintsCalculator();
        final List<NuGetPackageStructureAnalyser> analysers = Lists.newArrayList(frameworkConstraintsCalculator, packageItemsFactory);

        final InputStream inputStream = new BufferedInputStream(content);
        try {
            new NuGetPackageStructureVisitor(analysers).visit(inputStream);
        } finally {
            FileUtil.close(inputStream);
        }

        final Map<String, String> metadata = packageItemsFactory.getItems();
        metadata.put(TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(frameworkConstraintsCalculator.getPackageConstraints()));
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
