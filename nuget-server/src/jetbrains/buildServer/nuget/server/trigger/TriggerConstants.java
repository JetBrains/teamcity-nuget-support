/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 15:00
 */
public interface TriggerConstants {
  public static final String TRIGGER_ID = "nuget.simple";

  //TODO: use same extensions as for psexec and handle
  public static final String NUGET_EXE = "nuget.exe";
  public static final String SOURCE = "nuget.source";
  public static final String PACKAGE = "nuget.package";
  public static final String VERSION = "nuget.version";
  public static final String INCLUDE_PRERELEASE = "nuget.include.prerelease";
  public static final String USERNAME = "nuget.username";
  public static final String PASSWORD = "nuget.password";
}
