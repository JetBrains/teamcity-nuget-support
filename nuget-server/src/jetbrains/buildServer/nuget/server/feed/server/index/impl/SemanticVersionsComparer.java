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

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Created 18.03.13 15:50
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SemanticVersionsComparer  {
  @NotNull
  public static Comparator<NuGetPackageBuilder> getBuildersComparator() {
    return new Comparator<NuGetPackageBuilder>() {
      private final Comparator<String> myVersionComparator = getSemanticVersionsComparator();

      public int compare(@NotNull NuGetPackageBuilder o1, @NotNull NuGetPackageBuilder o2) {
        final String id1 = o1.getPackageName();
        final String id2 = o2.getPackageName();

        int cmp;
        if ((cmp = id1.compareTo(id2)) != 0) return cmp;

        final String v1 = o1.getVersion();
        final String v2 = o2.getVersion();
        return myVersionComparator.compare(v1, v2);
      }
    };
  }

  @NotNull
  public static Comparator<NuGetIndexEntry> getEntriesComparator() {
    return new Comparator<NuGetIndexEntry>() {
      private final Comparator<String> myVersionComparator = getSemanticVersionsComparator();

      public int compare(@NotNull NuGetIndexEntry o1, @NotNull NuGetIndexEntry o2) {
        final String id1 = o1.getPackageInfo().getId();
        final String id2 = o2.getPackageInfo().getId();

        int cmp;
        if ((cmp = id1.compareTo(id2)) != 0) return cmp;

        final String v1 = o1.getPackageInfo().getVersion();
        final String v2 = o2.getPackageInfo().getVersion();

        return myVersionComparator.compare(v1, v2);
      }
    };
  }

  @NotNull
  public static Comparator<String> getSemanticVersionsComparator() {
    return new Comparator<String>() {

      public int compare(@NotNull String v1, @NotNull String v2) {
        if (v1.equals(v2)) return 0;

        final ParsedVersion p1 = new ParsedVersion(v1);
        final ParsedVersion p2 = new ParsedVersion(v2);

        String[] o1 = split(p1.getInitialPart());
        String[] o2 = split(p2.getInitialPart());

        int x;
        for(int i = 0, max = Math.min(o1.length, o2.length); i < max; i++) {
          if ((x = compareElements(o1[i], o2[i]))!= 0) return x;
        }
        if (o1.length < o2.length) return -1;
        if (o1.length > o2.length) return 1;


        o1 = split(p1.getMunisPart());
        o2 = split(p2.getMunisPart());

        for(int i = 0, max = Math.min(o1.length, o2.length); i < max; i++) {
          if ((x = compareElements(o1[i], o2[i]))!= 0) return x;
        }
        if (o1.length == 0 && o2.length > 0) return 1;
        if (o2.length == 0 && o1.length > 0) return -1;

        if (o1.length < o2.length) return -1;
        if (o1.length > o2.length) return 1;

        o1 = split(p1.getPlusPart());
        o2 = split(p2.getPlusPart());

        for(int i = 0, max = Math.min(o1.length, o2.length); i < max; i++) {
          if ((x = compareElements(o1[i], o2[i]))!= 0) return x;
        }
        if (o1.length < o2.length) return -1;
        if (o1.length > o2.length) return 1;
        return 0;
      }

      @NotNull
      private String[] split(@Nullable String s) {
        if (s == null || s.length() == 0) return new String[0];
        return Pattern.compile("\\.").split(s);
      }

      private int compareElements(@NotNull String s1, @NotNull String s2) {
        int i1 = 0;
        int i2 = 0;
        boolean isInt1 = true;
        boolean isInt2 = true;
        try {
          i1 = Integer.parseInt(s1);
        } catch (Exception e) {
          isInt1 = false;
        }
        try {
          i2 = Integer.parseInt(s2);
        } catch (Exception e) {
          isInt2 = false;
        }

        if (isInt1 && isInt2) {
          if (i1 == i2) return 0;
          if (i1 < i2) return -1;
          if (i1 > i2) return 1;
        }

        if (isInt1 && !isInt2) {
          return -1;
        }

        if (!isInt1 && isInt2) {
          return 1;
        }

        return s1.compareTo(s2);
      }
    };
  }


  public static class ParsedVersion {
    private final String myData;
    private final int myDash;
    private final int myPlus;

    public ParsedVersion(@NotNull final String data) {
      myData = data;
      myDash = data.indexOf('-');
      myPlus = data.indexOf('+');
    }

    @NotNull
    public String getInitialPart() {
      int endOffset = myData.length();
      if (myPlus >= 0) endOffset = Math.min(myPlus, endOffset);
      if (myDash >= 0) endOffset = Math.min(myDash, endOffset);

      return myData.substring(0, endOffset);
    }

    @Nullable
    public String getMunisPart() {
      if (myDash < 0) return null;

      int endOffset = myData.length();
      if (myPlus >= 0) endOffset = Math.min(myPlus, endOffset);

      return myData.substring(myDash + 1, endOffset);
    }

    @Nullable
    public String getPlusPart() {
      if (myPlus < 0) return null;
      return myData.substring(myPlus + 1);
    }
  }
}
