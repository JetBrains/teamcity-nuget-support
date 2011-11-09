/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.util;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 09.11.11 20:57
 */
public class SystemInfoImpl implements SystemInfo {
  public boolean isWindows() {
    return com.intellij.openapi.util.SystemInfo.isWindows;
  }
}
