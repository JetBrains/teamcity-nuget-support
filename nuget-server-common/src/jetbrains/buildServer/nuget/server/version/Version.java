/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.version;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeniy.Koshkin
 */
public class Version implements Comparable<Version> {
  private static final String FORMAT = "(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(?:\\.)?(\\d*)";
  private static final Pattern PATTERN = Pattern.compile(FORMAT);

  public static final Version EMPTY = new Version(0, 0, 0, 0);

  private final int myMajor;
  private final int myMinor;
  private final int myPatch;
  private final int myBuild;

  public Version(final int major, final int minor, final int patch ) {
    this( major, minor, patch, 0 );
  }

  public Version(final int major, final int minor, final int patch, final int build ){
    this.myMajor = major;
    this.myMinor = minor;
    this.myPatch = patch;
    this.myBuild = build;
  }

  @Nullable
  public static Version valueOf(@Nullable final String version) {
    if(version == null) return null;

    final Matcher matcher = PATTERN.matcher( version );
    if (!matcher.matches())
      return null;

    final int major = Integer.valueOf( matcher.group( 1 ) );
    final int minor = Integer.valueOf( matcher.group( 2 ) );
    final int patch;
    final String patchMatch = matcher.group( 3 );
    if (!StringUtil.isEmpty(patchMatch))
      patch = Integer.valueOf(patchMatch);
    else
      patch = 0;

    final int build;
    final String buildMatch = matcher.group( 4 );
    if (!StringUtil.isEmpty(buildMatch) )
      build = Integer.valueOf(buildMatch);
    else
      build = 0;

    return new Version( major, minor, patch, build );
  }

  public int getMajor() {
    return myMajor;
  }

  public int getMinor() {
    return myMinor;
  }

  public int getPatch() {
    return myPatch;
  }

  public int getBuild() {
    return myBuild;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 43 * hash + this.myMajor;
    hash = 43 * hash + this.myMinor;
    hash = 43 * hash + this.myPatch;
    hash = 43 * hash + this.myBuild;
    return hash;
  }

  @Override
  public boolean equals( @Nullable final Object object ){
    if (!(object instanceof Version)) return false;
    final Version other = (Version) object;
    return !(other.myMajor != this.myMajor || other.myMinor != this.myMinor || other.myPatch != this.myPatch || other.myBuild != this.myBuild);
  }

  public int compareTo( @NotNull final Version other ){
    if (equals(other)) return 0;
    if (this.myMajor < other.myMajor) return -1;
    else if (this.myMajor == other.myMajor)
      if (this.myMinor < other.myMinor) return -1;
      else if (this.myMinor == other.myMinor) if (this.myPatch < other.myPatch) return -1;
      else if (this.myPatch == other.myPatch && this.myBuild < other.myBuild) return -1;
    return 1;
  }

  @Override
  public String toString(){
    return String.valueOf(this.myMajor) + "." + this.myMinor + "." + this.myPatch + "." + this.myBuild;
  }
}
