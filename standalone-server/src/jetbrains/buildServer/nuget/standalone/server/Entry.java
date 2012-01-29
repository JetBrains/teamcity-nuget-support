/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.standalone.server;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:44
 */
public class Entry {
  private final long myId;
  private final Map<String, String> myMap;

  public Entry(long id, @NotNull final Map<String, String> map) {
    myId = id;
    myMap = new TreeMap<String, String>(map);
  }

  @NotNull
  public String getKey() {
    return myMap.get("Id") + "." + myMap.get("Version");
  }

  public long getId() {
    return myId;
  }

  @NotNull
  public Map<String,String> getMap() {
    return myMap;
  }
}
