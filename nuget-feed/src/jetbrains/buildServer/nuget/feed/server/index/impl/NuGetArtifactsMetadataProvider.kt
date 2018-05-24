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
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_ARTIFACT_RELPATH
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.ID
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.NORMALIZED_VERSION
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.impl.LogUtil
import jetbrains.buildServer.serverSide.metadata.BuildMetadataProvider
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter

/**
 * Writes NuGet packages metadata into affected NuGet feeds.
 */
class NuGetArtifactsMetadataProvider(private val myReset: ResponseCacheReset,
                                     private val myFeedSettings: NuGetServerSettings,
                                     private val myMetadataProvider: NuGetBuildMetadataProvider,
                                     private val myFeedsProvider: NuGetBuildFeedsProvider,
                                     private val myMetadataStorage: MetadataStorage) : BuildMetadataProvider {

    override fun getProviderId() = PackageConstants.NUGET_PROVIDER_ID

    override fun generateMedatadata(build: SBuild, store: MetadataStorageWriter) {
        if (!myFeedSettings.isNuGetServerEnabled) {
            LOG.debug(String.format("Skip NuGet metadata generation for build %s. NuGet feed is disabled.", LogUtil.describe(build)))
            return
        }

        val targetFeeds = myFeedsProvider.getFeeds(build)
        if (targetFeeds.isEmpty()) {
            LOG.debug("No NuGet feeds found to index packages from build ${LogUtil.describe(build)}")
            return
        }

        LOG.debug("Looking for NuGet packages in ${LogUtil.describe(build)}")

        val packages = myMetadataProvider.getPackagesMetadata(build)
        packages.forEach { metadata ->
            val id = metadata[ID]
            val version = metadata[NORMALIZED_VERSION]
            if (id != null && version != null) {
                val key = NuGetUtils.getPackageKey(id, version)
                targetFeeds.forEach { feedData ->
                    if (feedData.key == providerId) {
                        store.addParameters(key, metadata)
                    } else {
                        myMetadataStorage.addBuildEntry(build.buildId, feedData.key, key, metadata, !build.isPersonal)
                    }
                    LOG.info("Added NuGet package $key from build ${LogUtil.describe(build)} into feed $feedData")
                }
            } else {
                LOG.warn("Failed to resolve NuGet package Id, package ignored: ${metadata[TEAMCITY_ARTIFACT_RELPATH]}")
            }
        }

        if (packages.isNotEmpty()) {
            myReset.resetCache()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetArtifactsMetadataProvider::class.java.name)
    }
}
