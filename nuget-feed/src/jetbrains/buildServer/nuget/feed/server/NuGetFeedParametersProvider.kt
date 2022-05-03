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

import jetbrains.buildServer.BuildAuthUtil
import jetbrains.buildServer.agent.AgentRuntimeProperties
import jetbrains.buildServer.nuget.common.NuGetServerConstants.*
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider
import jetbrains.buildServer.web.util.WebUtil

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.10.11 17:52
 */
class NuGetFeedParametersProvider(private val mySettings: NuGetServerSettings,
                                  private val myProjectManager: ProjectManager,
                                  private val myRepositoryManager: RepositoryManager,
                                  private val myLoginConfiguration: LoginConfiguration)
    : AbstractBuildParametersProvider(), BuildStartContextProcessor {


    override fun getParameters(build: SBuild, emulationMode: Boolean): Map<String, String> {
        if (!mySettings.isNuGetServerEnabled) {
            return emptyMap()
        }

        val properties = mutableMapOf<String, String>()
        build.buildType?.let {
            properties.putAll(getBuildTypeParameters(it))
        }

        if (build is SRunningBuild) {
            if (isFallbackModeEnabled()) {
                build.buildType?.let {
                    properties.putAll(getFallbackParameters(it))
                }
            }
            val buildToken = String.format("%s:%s", BuildAuthUtil.makeUserId(build.getBuildId()), build.agentAccessCode)
            properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = EncryptUtil.scramble(buildToken)
        } else {
            properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = ""
        }

        return properties
    }

    private fun isFallbackModeEnabled() =
        TeamCityProperties.getBoolean(FEED_PARAMETERS_PROVIDER_ENABLE_FALLBACK_PARAMETERS_FOR_RUNNING_BUILD)

    override fun getPrefix(): String {
        if (isFallbackModeEnabled()) return ""
        return FEED_REF_PREFIX
    }

    override fun updateParameters(context: BuildStartContext) {
        if (!mySettings.isNuGetServerEnabled) {
            return
        }

        context.build.buildType?.let {
            getFallbackParameters(it).forEach { key, value ->
                context.addSharedParameter(key, value)
            }
        }
    }

    fun getBuildTypeParameters(buildType: SBuildType): Map<String, String> {
        if (!mySettings.isNuGetServerEnabled) {
            return emptyMap()
        }

        val parameters = mutableMapOf<String, String>()
        val repositories = myRepositoryManager
                .getRepositories(buildType.project, true)
                .filterIsInstance<NuGetRepository>()

        val authTypes = getAuthTypes()
        repositories.forEach { repository ->
            val project = myProjectManager.findProjectById(repository.projectId) ?: return@forEach
            NuGetAPIVersion.values().forEach { version ->
                val feedPath = NuGetUtils.getProjectFeedPath(project.externalId, repository.name, version)
                authTypes.forEach { authType ->
                    val referenceName = NuGetUtils.getProjectFeedReference(
                            authType, project.externalId, repository.name, version
                    )
                    val reference = ReferencesResolverUtil.makeReference(AgentRuntimeProperties.TEAMCITY_SERVER_URL) +
                            WebUtil.combineContextPath("/$authType/", feedPath)
                    parameters[referenceName] = reference
                }
            }
        }

        return parameters
    }

    private fun getFallbackParameters(buildType: SBuildType): Map<String, String> {
        val parameters = mutableMapOf<String, String>()

        // Add fallback for global nuget feed parameters
        buildType.undefinedParameters.let { undefinedParameters ->
            "teamcity.nuget.feed.server".let {
                if (undefinedParameters.contains(it)) {
                    parameters[it] = ReferencesResolverUtil.makeReference("teamcity.nuget.feed.guestAuth._Root.default.v2")
                }
            }
            "teamcity.nuget.feed.auth.server".let {
                if (undefinedParameters.contains(it)) {
                    parameters[it] = ReferencesResolverUtil.makeReference("teamcity.nuget.feed.httpAuth._Root.default.v2")
                }
            }
            "system.teamcity.nuget.feed.auth.serverRootUrlBased.server".let {
                if (undefinedParameters.contains(it)) {
                    parameters[it] = ReferencesResolverUtil.makeReference("teamcity.nuget.feed.httpAuth._Root.default.v2")
                }
            }
        }

        return parameters
    }

    private fun getAuthTypes() : Set<String> {
        return if (myLoginConfiguration.isGuestLoginAllowed) {
            setOf("httpAuth", "guestAuth")
        } else {
            setOf("httpAuth")
        }
    }
}
