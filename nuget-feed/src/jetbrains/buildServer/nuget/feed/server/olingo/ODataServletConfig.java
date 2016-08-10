/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.olingo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * Configuration for ODataService.
 */
public class ODataServletConfig implements ServletConfig {
  @Override
  public String getServletName() {
    return "NuGet Feed";
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public String getInitParameter(String name) {
    return null;
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return null;
  }
}
