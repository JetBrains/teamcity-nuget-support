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

package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.agent.Constants
import jetbrains.buildServer.nuget.common.NuGetServerConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider

import javax.ws.rs.core.UriBuilder
import java.util.HashMap

import jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL
import jetbrains.buildServer.agent.ServerProvidedProperties.TEAMCITY_AUTH_PASSWORD_PROP
import jetbrains.buildServer.agent.ServerProvidedProperties.TEAMCITY_AUTH_USER_ID_PROP
import jetbrains.buildServer.nuget.common.NuGetServerConstants.*
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.parameters.ReferencesResolverUtil.makeReference
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.util.WebUtil.*

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.10.11 17:52
 */
class NuGetServerPropertiesProvider(private val mySettings: NuGetServerSettings,
                                    private val myProjectManager: ProjectManager,
                                    private val myRepositoryManager: RepositoryManager,
                                    private val myRootUrlHolder: RootUrlHolder)
    : AbstractBuildParametersProvider() {

    override fun getParameters(build: SBuild, emulationMode: Boolean): Map<String, String> {
        val properties = getFeedProperties(build)
        if (!mySettings.isNuGetServerEnabled) {
            return properties
        }

        val buildToken = String.format("%s:%s",
            makeReference(TEAMCITY_AUTH_USER_ID_PROP),
            makeReference(TEAMCITY_AUTH_PASSWORD_PROP))

        val result = build.valueResolver.resolve(buildToken)
        if (result.isFullyResolved) {
            properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = EncryptUtil.scramble(result.result)
        }

        val buildProject = myProjectManager.findProjectById(build.projectId)
        if (build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).isNotEmpty() ||
            NuGetIndexUtils.isIndexingEnabledForBuild(build) &&
            NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).any()) {
            properties[NuGetServerConstants.FEED_AGENT_SIDE_INDEXING] = "true"
        }

        return properties
    }

    val properties: MutableMap<String, String>
        get() {
            val map = HashMap<String, String>()
            // Add global feed
            val rootProject = myProjectManager.rootProject
            val feedData = NuGetFeedData.DEFAULT
            if (myRepositoryManager.hasRepository(rootProject, PackageConstants.NUGET_PROVIDER_ID, feedData.feedId)) {
                val feedPath = NuGetUtils.getProjectFeedPath(feedData.projectId, feedData.feedId)
                map[FEED_REF_GUEST_AUTH_GLOBAL] = makeReference(TEAMCITY_SERVER_URL) + combineContextPath(GUEST_AUTH_PREFIX, feedPath)
                val httpAuthFeedPath = combineContextPath(HTTP_AUTH_PREFIX, feedPath)
                map[FEED_REF_HTTP_AUTH_GLOBAL] = makeReference(TEAMCITY_SERVER_URL) + httpAuthFeedPath
                map[FEED_REF_HTTP_AUTH_PUBLIC_GLOBAL] = UriBuilder
                    .fromUri(myRootUrlHolder.rootUrl)
                    .replacePath(httpAuthFeedPath).build().toString()
            }
            return map
        }

    private fun getFeedProperties(build: SBuild): MutableMap<String, String> {
        val map = properties
        myProjectManager.findProjectById(build.projectId)?.let {
            val repositories = myRepositoryManager
                .getRepositories(it, true)
                .filterIsInstance<NuGetRepository>()

            repositories.forEach {
                val feedPath = NuGetUtils.getProjectFeedPath(it.projectId, it.name)
                val feedSuffix = if (it.name == NuGetFeedData.DEFAULT_FEED_ID) it.projectId else "${it.projectId}.${it.name}"
                val httpAuthFeedPath = combineContextPath(HTTP_AUTH_PREFIX, feedPath)
                map[FEED_REF_PREFIX + feedSuffix + FEED_REF_URL_SUFFIX] = makeReference(TEAMCITY_SERVER_URL) + httpAuthFeedPath
                map[FEED_REF_PREFIX + feedSuffix + FEED_REF_PUBLIC_URL_SUFFIX] = UriBuilder
                    .fromUri(myRootUrlHolder.rootUrl)
                    .replacePath(httpAuthFeedPath).build().toString()
            }
        }
        return map
    }
}
