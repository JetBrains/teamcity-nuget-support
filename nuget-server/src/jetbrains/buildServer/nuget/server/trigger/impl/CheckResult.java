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

package jetbrains.buildServer.nuget.server.trigger.impl;

import com.intellij.util.Function;
import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 15:40
 */
public class CheckResult {
  @Nullable private final Collection<SourcePackageInfo> myInfos;
  @Nullable private final String myError;

  private CheckResult(@Nullable final Collection<SourcePackageInfo> infos,
                      @Nullable final String error) {
    myInfos = infos;
    myError = error;
  }

  @NotNull
  public static CheckResult failed(@Nullable final String error) {
    return new CheckResult(null, error == null ? "Error" : error);
  }

  @NotNull
  public static CheckResult empty() {
    return fromResult(Collections.<SourcePackageInfo>emptyList());
  }

  @NotNull
  public static CheckResult fromResult(@NotNull Collection<SourcePackageInfo> data) {
    return new CheckResult(data, null);
  }

  @NotNull
  public static CheckResult fromResult(@Nullable final ListPackagesResult infos) {
    if (infos == null) {
      return fromResult(Collections.<SourcePackageInfo>emptyList());
    }
    String error = infos.getErrorMessage();
    if (!StringUtil.isEmptyOrSpaces(error)) {
      return failed(error);
    }
    return fromResult(infos.getCollectedInfos());
  }

  @NotNull
  public Collection<SourcePackageInfo> getInfos() {
    if (myError != null || myInfos == null) throw new RuntimeException(myError);
    return myInfos;
  }

  @Nullable
  public String getError() {
    return myError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CheckResult that = (CheckResult) o;

    if (myError != null ? !myError.equals(that.myError) : that.myError != null) return false;
    if (myInfos != null ? !myInfos.equals(that.myInfos) : that.myInfos != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInfos != null ? myInfos.hashCode() : 0;
    result = 31 * result + (myError != null ? myError.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    if (myError != null || myInfos == null) return "CheckResult{ " + myError + " }";
    if (myInfos.isEmpty()) return "CheckResult{ [] }";
    else return "CheckResult{ " + StringUtil.join(myInfos, TO_STRING, ", " + "}");
  }

  private static final Function<SourcePackageInfo, String> TO_STRING = new Function<SourcePackageInfo, String>() {
    public String fun(SourcePackageInfo sourcePackageInfo) {
      return sourcePackageInfo.toString();
    }
  };
}
