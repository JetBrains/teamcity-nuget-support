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
import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL
import jetbrains.buildServer.nuget.common.NuGetServerConstants.*
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.parameters.ReferencesResolverUtil.makeReference
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider
import jetbrains.buildServer.web.util.WebUtil.*
import java.util.*
import javax.ws.rs.core.UriBuilder

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
        if (!mySettings.isNuGetServerEnabled) {
            return emptyMap()
        }

        val properties = properties
        if (build is SRunningBuild) {
            val buildToken = String.format("%s:%s", BuildAuthUtil.makeUserId(build.getBuildId()), build.agentAccessCode)
            properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = EncryptUtil.scramble(buildToken)
        } else {
            properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = ""
        }

        return properties
    }

    val properties: MutableMap<String, String>
        get() {
            return mutableMapOf()
        }
}
