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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.06.12 16:12
 */
public class PackageSourceImpl implements PackageSource {
  private final String mySource;
  private final String myUserName;
  private final String myPassword;

  public PackageSourceImpl(@NotNull final String source,
                           @Nullable final String userName,
                           @Nullable final String password) {
    mySource = source;
    myUserName = userName;
    myPassword = password;
  }

  public PackageSourceImpl(@NotNull final String source) {
    this(source, null, null);
  }

  @NotNull
  public String getSource() {
    return mySource;
  }

  public String getUserName() {
    return myUserName;
  }

  public String getPassword() {
    return myPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PackageSourceImpl)) return false;

    PackageSourceImpl that = (PackageSourceImpl) o;

    if (myPassword != null ? !myPassword.equals(that.myPassword) : that.myPassword != null) return false;
    if (!mySource.equals(that.mySource)) return false;
    if (myUserName != null ? !myUserName.equals(that.myUserName) : that.myUserName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return mySource.hashCode();
  }

  @NotNull
  public static List<PackageSource> convert(@NotNull String... rawSources) {
    return convert(Arrays.asList(rawSources));
  }

  @NotNull
  public static List<PackageSource> convert(@NotNull Collection<String> rawSources) {
    final List<PackageSource> sources = new ArrayList<PackageSource>();
    for (String source : rawSources) {
      sources.add(new PackageSourceImpl(source));
    }
    return sources;
  }
}
