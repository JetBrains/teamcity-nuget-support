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

package jetbrains.buildServer.nuget.server.toolRegistry.tab;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 13:57
 */
public enum LocalToolState {
  INSTALLED("Installed", false),
  INSTALLING("Installing", true),
  ;

  private final String myName;
  private final boolean myContainsMessages;

  LocalToolState(String name, boolean containsMessages) {
    myName = name;
    myContainsMessages = containsMessages;
  }

  public boolean isInstalled() {
    return this == INSTALLED;
  }

  public boolean isInstalling() {
    return this == INSTALLING;
  }

  public String getName() {
    return myName;
  }

  public boolean isContainsMessages() {
    return myContainsMessages;
  }
}
