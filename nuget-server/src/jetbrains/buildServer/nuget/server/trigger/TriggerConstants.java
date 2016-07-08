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
  String TRIGGER_ID = "nuget.simple";
  String NUGET_PATH_PARAM_NAME = "nuget.exe";
  
  String SOURCE = "nuget.source";
  String PACKAGE = "nuget.package";
  String VERSION = "nuget.version";
  String INCLUDE_PRERELEASE = "nuget.include.prerelease";
  String USERNAME = "nuget.username";
  String PASSWORD = "nuget.password";
}
