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

package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.common.FeedConstants
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.index.*
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_ARTIFACT_RELPATH
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_BUILD_TYPE_ID
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.serverSide.impl.LogUtil
import jetbrains.buildServer.serverSide.metadata.BuildMetadataProvider
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.IOException
import java.util.*

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.10.11 12:21
 */
class NuGetArtifactsMetadataProvider(private val myReset: ResponseCacheReset,
                                     private val myFeedSettings: NuGetServerSettings,
                                     private val myPackageAnalyzer: PackageAnalyzer,
                                     private val myProjectManager: ProjectManager,
                                     private val myRepositoryManager: RepositoryManager,
                                     private val myMetadataStorage: MetadataStorage) : BuildMetadataProvider {

    override fun getProviderId() = PackageConstants.NUGET_PROVIDER_ID

    override fun generateMedatadata(build: SBuild, store: MetadataStorageWriter) {
        if (!myFeedSettings.isNuGetServerEnabled) {
            LOG.debug(String.format("Skip NuGet metadata generation for build %s. NuGet feed disabled.", LogUtil.describe(build)))
            return
        }

        val targetFeeds = hashSetOf<NuGetFeedData>()
        if (myFeedSettings.isGlobalIndexingEnabled) {
            if (NuGetIndexUtils.isIndexingEnabledForBuild(build)) {
                val rootProject = myProjectManager.rootProject
                val globalFeed = NuGetFeedData.GLOBAL
                if (myRepositoryManager.hasRepository(rootProject, providerId, globalFeed.feedId)) {
                    targetFeeds.add(globalFeed)
                } else {
                    LOG.warn("Could not find '${globalFeed.feedId}' NuGet feed for '${globalFeed.projectId}' project.")
                }
            } else {
                LOG.debug("Indexing NuGet packages into build ${LogUtil.describe(build)} is disabled")
            }
        }

        build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach {feature ->
            feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED_ID]?.let {
                NuGetUtils.feedIdToData(it)?.let {
                    val project = myProjectManager.findProjectByExternalId(it.projectId)
                    if (project != null && myRepositoryManager.hasRepository(project, providerId, it.feedId)) {
                        targetFeeds.add(it)
                    } else {
                        LOG.warn("Could not find '${it.feedId}' NuGet feed for '${it.projectId}' project.")
                    }
                }
            }
        }

        if (targetFeeds.isEmpty()) {
            LOG.debug("No NuGet feeds found to index packages from build ${LogUtil.describe(build)}")
            return
        }

        LOG.debug("Looking for NuGet packages in ${LogUtil.describe(build)}")

        val packages = getPackagesMetadata(build)
        packages.forEach { metadata ->
            val id = metadata[ID]
            val version = metadata[NORMALIZED_VERSION]
            if (id != null && version != null) {
                val key = NuGetUtils.getPackageKey(id, version)
                targetFeeds.forEach { feedData ->
                    myMetadataStorage.addBuildEntry(build.buildId, feedData.key, key, metadata, !build.isPersonal)
                    LOG.debug("Added NuGet package $key from build ${LogUtil.describe(build)} into feed $feedData")
                }
            } else {
                LOG.warn("Failed to resolve NuGet package Id, package ignored: ${metadata[TEAMCITY_ARTIFACT_RELPATH]}")
            }
        }

        if (packages.isNotEmpty()) {
            myReset.resetCache()
        }
    }

    private fun getPackagesMetadata(build: SBuild): Collection<Map<String, String>> {
        readBuildMetadata(build)?.let {
            return it
        }

        val metadata = indexBuildPackages(build)
        writeBuildMetadata(build, metadata)
        return metadata
    }

    private fun indexBuildPackages(build: SBuild): List<Map<String, String>> {
        val nugetArtifacts = HashSet<BuildArtifact>()
        val artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

        visitArtifacts(artifacts.rootArtifact, nugetArtifacts)

        return nugetArtifacts.mapNotNull {
            LOG.info("Indexing NuGet package from artifact " + it.relativePath + " of build " + LogUtil.describe(build))
            try {
                generateMetadataForPackage(build, it)
            } catch (e: PackageLoadException) {
                LOG.warnAndDebugDetails("Failed to read NuGet package $it", e)
                null
            } catch (e: Throwable) {
                LOG.warnAndDebugDetails("Unexpected error while indexing NuGet package $it", e)
                null
            }
        }
    }

    private fun writeBuildMetadata(build: SBuild, metadata: List<Map<String, String>>) {
        if (metadata.isNotEmpty()) {
            val packages = metadata.mapNotNull {
                it[TEAMCITY_ARTIFACT_RELPATH]?.let { path ->
                    NuGetPackageData(path, it)
                }
            }
            File(build.artifactsDirectory, PackageConstants.PACKAGES_FILE_PATH).outputStream().use {
                NuGetPackagesUtil.writePackages(NuGetPackagesList(packages), it)
            }
        }
    }

    private fun readBuildMetadata(build: SBuild): Collection<Map<String, String>>? {
        val artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
        val artifact = artifacts.getArtifact(PackageConstants.PACKAGES_FILE_PATH)
        if (artifact != null) {
            val packages = try {
                artifact.inputStream.use {
                    NuGetPackagesUtil.readPackages(it)
                }
            } catch (e: IOException) {
                LOG.warnAndDebugDetails("Failed to read NuGet packages list", e)
                null
            }

            packages?.packages?.let {
                return it.values
            }

            FileUtil.delete(File(build.artifactsDirectory, PackageConstants.PACKAGES_FILE_PATH))
        }
        return null
    }

    private fun generateMetadataForPackage(build: SBuild, artifact: BuildArtifact): Map<String, String> {
        val metadata = try {
            artifact.inputStream.use {
                myPackageAnalyzer.analyzePackage(it)
            }
        } catch (e: IOException) {
            throw PackageLoadException("Failed to read build artifact data", e)
        }

        metadata[PACKAGE_SIZE] = artifact.size.toString()
        metadata[TEAMCITY_ARTIFACT_RELPATH] = artifact.relativePath
        metadata[TEAMCITY_BUILD_TYPE_ID] = build.buildTypeId

        try {
            artifact.inputStream.use {
                metadata[PACKAGE_HASH] = myPackageAnalyzer.getSha512Hash(it)
                metadata[PACKAGE_HASH_ALGORITHM] = PackageAnalyzer.SHA512
            }
        } catch (e: IOException) {
            throw PackageLoadException("Failed to calculate package hash", e)
        }

        val finishDate = build.finishDate
        val created = ODataDataFormat.formatDate(finishDate ?: Date())
        metadata[CREATED] = created
        metadata[LAST_UPDATED] = created
        metadata[PUBLISHED] = created

        return metadata
    }

    private fun visitArtifacts(artifact: BuildArtifact, packages: MutableSet<BuildArtifact>) {
        if (!artifact.isDirectory) {
            if (FeedConstants.PACKAGE_FILE_NAME_FILTER.accept(artifact.name)) {
                packages.add(artifact)
            }
            return
        }

        for (children in artifact.children) {
            visitArtifacts(children, packages)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetArtifactsMetadataProvider::class.java.name)
    }
}
