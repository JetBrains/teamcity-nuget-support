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

package jetbrains.buildServer.nuget.server.trigger.impl.source;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.util.RecentEntriesCache;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 13:11
 */
public class NuGetSourceCheckerImpl implements NuGetSourceChecker {
  private static final Logger LOG = Logger.getInstance(NuGetSourceCheckerImpl.class.getName());
  private final TimeService myTimeService;
  private final PackageCheckerSettings mySettings;
  private final PackageSourceChecker myChecker;
  private final RecentEntriesCache<String, CacheResult> myCache = new RecentEntriesCache<String, CacheResult>(500);

  public NuGetSourceCheckerImpl(@NotNull TimeService timeService,
                                @NotNull PackageCheckerSettings settings,
                                @NotNull PackageSourceChecker checker) {
    myTimeService = timeService;
    mySettings = settings;
    myChecker = checker;
  }

  @NotNull
  public Collection<CheckablePackage> getAccessiblePackages(@NotNull Collection<CheckablePackage> allPackages) {
    final List<CheckablePackage> checkable = new ArrayList<CheckablePackage>();
    for (CheckablePackage pkg : allPackages) {
      final CheckablePackage source = checkPackageSource(pkg);
      if (source != null) {
        checkable.add(source);
      }
    }
    return checkable;
  }

  @Nullable
  private CheckablePackage checkPackageSource(@NotNull CheckablePackage pkg) {
    final String source = pkg.getPackage().getSource();
    if (source == null) return pkg;

    CacheResult result = myCache.lookupOrCompute(source, COMPUTE_CACHE);
    if (result.isExpired()) {
      myCache.remove(source);
      result = myCache.lookupOrCompute(source, COMPUTE_CACHE);
    }

    if (result.getErrorText() != null) {
      pkg.setResult(CheckResult.failed(result.getErrorText()));
      return null;
    }
    return pkg;
  }

  private final RecentEntriesCache.Function<String, CacheResult> COMPUTE_CACHE
          = new RecentEntriesCache.Function<String, CacheResult>() {
    @NotNull
    public CacheResult fun(@NotNull String key) {
      try {
        return new CacheResult(myChecker.checkSource(key));
      } catch (final Throwable e) {
        LOG.warn("Failed to connect to " + key + ". " + e.getMessage(), e);
        return new CacheResult("Package feed is empty or inaccessible. " + e.getMessage());
      }
    }
  };

  private class CacheResult {
    private final long myExpireTime;
    private final String myErrorText;

    private CacheResult(@Nullable String errorText) {
      myExpireTime = myTimeService.now();
      myErrorText = errorText;
    }

    public boolean isExpired() {
      return myTimeService.now() > myExpireTime + mySettings.getPackageSourceAvailabilityCheckInterval();
    }

    @Nullable
    public String getErrorText() {
      return myErrorText;
    }
  }
}
