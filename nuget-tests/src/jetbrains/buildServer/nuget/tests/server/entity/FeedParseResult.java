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

package jetbrains.buildServer.nuget.tests.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 13.01.12 13:26
 */
public class FeedParseResult {
  private final Set<String> myProperties;
  private final Map<String, String> myAtomProperties;


  public FeedParseResult(@NotNull final Collection<String> properties,
                         @NotNull final Map<String, String> atomProperties) {
    myAtomProperties = new TreeMap<String, String>(atomProperties);
    myProperties = new TreeSet<String>(properties);
  }

  @NotNull
  public Set<String> getPropertyNames() {
    return myProperties;
  }

  @NotNull
  public Map<String, String> getAtomProperties() {
    return myAtomProperties;
  }
}
