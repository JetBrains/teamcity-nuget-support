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

package jetbrains.buildServer.nuget.server.trigger;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:41
 */
public class NamedPackagesUpdateChecker implements TriggerUpdateChecker {
  private static final Logger LOG = Logger.getInstance(NamedPackagesUpdateChecker.class.getName());

  public static final String KEY = "hash";

  private final PackageChangesManager myPackageChangesManager;
  private final TriggerRequestFactory myRequestFactory;
  private final PackagesHashCalculator myCalculator;


  public NamedPackagesUpdateChecker(@NotNull final PackageChangesManager packageChangesManager,
                                    @NotNull final TriggerRequestFactory requestFactory,
                                    @NotNull final PackagesHashCalculator calculator) {
    myPackageChangesManager = packageChangesManager;
    myRequestFactory = requestFactory;
    myCalculator = calculator;
  }

  @Nullable
  public BuildStartReason checkChanges(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    final PackageCheckRequest checkRequest = myRequestFactory.createRequest(context);

    CheckResult result;
    try {
      result = myPackageChangesManager.checkPackage(checkRequest);
      //no change available
    } catch (Throwable t) {
      LOG.warnAndDebugDetails("Failed to ckeck changes for package: " + checkRequest.getPackage().getPackageId() + ". " + t.getMessage(), t);
      result = CheckResult.failed(t.getMessage());
    }

    if (result == null) return null;
    final String error = result.getError();
    if (error != null) {
      throw new BuildTriggerException("Failed to check for package versions. " + error);
    }

    final CustomDataStorage storage = context.getCustomDataStorage();

    @NotNull  final String newHash = myCalculator.serializeHashcode(result.getInfos());
    @Nullable final String oldHash = storage.getValue(KEY);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Package: " + checkRequest.getPackage().toString());
      LOG.debug("Recieved packages hash: " + newHash);
      LOG.debug("          old hash was: " + oldHash);
    }

    if (oldHash == null || (!newHash.equals(oldHash) && !newHash.equals("v2"))) {
      storage.putValue(KEY, newHash);
      storage.flush();
    }

    //empty feed is error, not a trigger event,
    //still, we update trigger state for that
    if (result.getInfos().isEmpty()) {
      throw new BuildTriggerException("Failed to check for package versions. Package " + checkRequest.getPackage().getPackageId() + " was not found in the feed");
    }

    if (myCalculator.isUpgradeRequired(oldHash, newHash)) {
      return new BuildStartReason("NuGet Package " + checkRequest.getPackage().getPackageId() + " updated");
    }

    return null;
  }
}
