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
import jetbrains.buildServer.parameters.ReferencesResolverUtil.makeReference
import jetbrains.buildServer.web.util.WebUtil.*

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.10.11 17:52
 */
class NuGetServerPropertiesProvider(private val mySettings: NuGetServerSettings, private val myRootUrlHolder: RootUrlHolder) : AbstractBuildParametersProvider() {

    override fun getParameters(build: SBuild, emulationMode: Boolean): Map<String, String> {
        val properties = properties
        if (mySettings.isNuGetServerEnabled) {
            val buildToken = String.format("%s:%s",
                    makeReference(TEAMCITY_AUTH_USER_ID_PROP),
                    makeReference(TEAMCITY_AUTH_PASSWORD_PROP))

            val result = build.valueResolver.resolve(buildToken)
            if (result.isFullyResolved) {
                properties[FEED_REFERENCE_AGENT_API_KEY_PROVIDED] = EncryptUtil.scramble(result.result)
            }

            if (mySettings.isGlobalIndexingEnabled && NuGetIndexUtils.isIndexingEnabledForBuild(build)) {
                properties[NuGetServerConstants.FEED_INDEXING_ENABLED_PROP] = "true"
            }
        }
        return properties
    }

    val properties: MutableMap<String, String>
        get() {
            val map = HashMap<String, String>()
            if (mySettings.isNuGetServerEnabled) {
                val nugetFeedPath = NuGetServerSettings.GLOBAL_PATH
                map[FEED_REFERENCE_AGENT_PROVIDED] = makeReference(TEAMCITY_SERVER_URL) + combineContextPath(GUEST_AUTH_PREFIX, nugetFeedPath)
                val httpAuthFeedPath = combineContextPath(HTTP_AUTH_PREFIX, nugetFeedPath)
                map[FEED_AUTH_REFERENCE_AGENT_PROVIDED] = makeReference(TEAMCITY_SERVER_URL) + httpAuthFeedPath
                map[Constants.SYSTEM_PREFIX + FEED_AUTH_REFERENCE_SERVER_PROVIDED] = UriBuilder.fromUri(myRootUrlHolder.rootUrl).replacePath(httpAuthFeedPath).build().toString()
            }
            return map
        }
}
