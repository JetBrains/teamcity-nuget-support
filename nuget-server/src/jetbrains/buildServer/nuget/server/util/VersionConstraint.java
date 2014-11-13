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

package jetbrains.buildServer.nuget.server.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Evgeniy.Koshkin
 */
public class VersionConstraint {

  static Splitter VERSIONS_IN_RANGE = Splitter.on(",").omitEmptyStrings().trimResults();

  @Nullable private SemanticVersion myMinVersion;
  private boolean myIsMinInclusive = true;
  @Nullable private SemanticVersion myMaxVersion = null;
  private boolean myIsMaxInclusive = true;

  @Nullable
  public static VersionConstraint valueOf(@Nullable String value) {
    if(StringUtil.isEmpty(value)) return null;
    value = value.trim();
    final VersionConstraint versionSpec = new VersionConstraint();

    final SemanticVersion version = SemanticVersion.valueOf(value);
    if ( version != null )
    {
      versionSpec.myIsMinInclusive = true;
      versionSpec.myMinVersion = version;
      return versionSpec;
    }

    if ( value.length() < 3 ) return null;

    switch ( value.charAt( 0 ) )
    {
      case '[':
        versionSpec.myIsMinInclusive = true;
        break;
      case '(':
        versionSpec.myIsMinInclusive = false;
        break;
      default:
        return null;
    }

    switch ( value.charAt( value.length() - 1 ) )
    {
      case ']':
        versionSpec.myIsMaxInclusive = true;
        break;
      case ')':
        versionSpec.myIsMaxInclusive = false;
        break;
      default:
        return null;
    }

    final String valueTrimmed = value.substring(1, value.length() - 1);
    final String[] parts = Iterables.toArray(VERSIONS_IN_RANGE.split(valueTrimmed), String.class);
    String minVersionString = "";
    String maxVersionString = "";
    if(parts.length == 2){
      minVersionString = parts[0];
      maxVersionString = parts[1];
    } else if (parts.length == 1){
      if(valueTrimmed.indexOf(',') == -1){
        minVersionString = maxVersionString = parts[0];
      } else{
        if(valueTrimmed.startsWith(",")){
          maxVersionString = parts[0];
        } else if(valueTrimmed.endsWith(",")){
          minVersionString = parts[0];
        } else return null;
      }
    } else return null;

    SemanticVersion minVersion = SemanticVersion.valueOf(minVersionString);
    SemanticVersion maxVersion = SemanticVersion.valueOf(maxVersionString);

    if ( minVersion == null && maxVersion == null ) return null;

    versionSpec.myMinVersion = minVersion;
    versionSpec.myMaxVersion = maxVersion;

    return versionSpec;
  }

  public boolean satisfies(@NotNull SemanticVersion version) {
    boolean condition = true;
    if (myMinVersion != null) {
      if (myIsMinInclusive)
        condition = version.compareTo(myMinVersion) >= 0;
      else
        condition = version.compareTo(myMinVersion) > 0;
    }
    if (myMaxVersion != null) {
      if (myIsMaxInclusive)
        condition = condition && version.compareTo(myMaxVersion) <= 0;
      else
        condition = condition && version.compareTo(myMaxVersion) < 0;
    }
    return condition;
  }
}
