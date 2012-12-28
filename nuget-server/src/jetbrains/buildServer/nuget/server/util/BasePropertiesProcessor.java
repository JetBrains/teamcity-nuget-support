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

package jetbrains.buildServer.nuget.server.util;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 10:59
 */
public abstract class BasePropertiesProcessor implements PropertiesProcessor {
  @Nullable
  protected static String notEmpty(@NotNull final String key,
                                   @NotNull String errorMessage,
                                   @NotNull final Map<String, String> params,
                                   @NotNull final Collection<InvalidProperty> result) {
    final String value = params.get(key);
    if (StringUtil.isEmptyOrSpaces(value)) {
      result.add(new InvalidProperty(key, errorMessage));
      return null;
    }
    return value;
  }

  @NotNull
  public final Collection<InvalidProperty> process(final Map<String, String> properties) {
    Collection<InvalidProperty> result =  new ArrayList<InvalidProperty>();

    if (properties == null) return result;
    checkProperties(Collections.unmodifiableMap(properties), result);

    return result;
  }

  protected abstract void checkProperties(@NotNull final Map<String, String> map, @NotNull Collection<InvalidProperty> result);
}
