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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import com.google.common.collect.Sets;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkConstraints {

  private static final String SEPARATOR = "|";

  @NotNull
  public static String convertToString(Collection<String> constraints){
    return StringUtil.join("|", constraints);
  }

  @NotNull
  public static Set<String> convertFromString(@NotNull String string){
    return Sets.newHashSet(string.split(SEPARATOR));
  }
}
