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

package jetbrains.buildServer.nuget.feed.server.olingo

import java.util.*
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * Configuration for ODataService.
 */
class ODataServletConfig(private val parameters: Map<String, String>) : ServletConfig {
    override fun getServletName(): String {
        return "NuGet Feed"
    }

    override fun getServletContext(): ServletContext? {
        return null
    }

    override fun getInitParameter(name: String): String? {
        return parameters[name]
    }

    override fun getInitParameterNames(): Enumeration<String>? {
        return Vector(parameters.keys).elements()
    }
}
