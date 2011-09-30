/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:34
 */
public class PackageCheckEntry {
  private static final long REMOVE_IDLE_TRIGGER_SPAN_TIMES = 5;
  private final TimeService myTime;

  private final PackageCheckRequest myKey;
  private long myNextCheckTime;
  private long myRemoveTime;

  private final AtomicReference<CheckResult> myResult = new AtomicReference<CheckResult>();
  private final AtomicBoolean myExecuting = new AtomicBoolean();

  PackageCheckEntry(@NotNull final PackageCheckRequest key,
                    @NotNull final TimeService time) {
    myKey = key;
    myTime = time;
    updateTimes();
  }

  private void updateTimes() {
    long now = myTime.now();
    myNextCheckTime = now + myKey.getCheckInterval();
    myRemoveTime = now + myKey.getCheckInterval() * REMOVE_IDLE_TRIGGER_SPAN_TIMES;
  }

  public boolean forRequest(@NotNull final PackageCheckRequest request) {
    if (!equals(myKey.getMode(), request.getMode())) return false;
    if (!equals(myKey.getPackageId(), request.getPackageId())) return false;
    if (!equals(myKey.getPackageSource(), request.getPackageSource())) return false;
    if (!equals(myKey.getVersionSpec(), request.getVersionSpec())) return false;
    return true;
  }

  public void update(@NotNull final PackageCheckRequest request) {
    if (!forRequest(request)) throw new IllegalArgumentException("Incompatible request");

    long now = myTime.now();
    myNextCheckTime = Math.min(myNextCheckTime, now + request.getCheckInterval());
    myRemoveTime = Math.max(myRemoveTime, now + request.getCheckInterval() * REMOVE_IDLE_TRIGGER_SPAN_TIMES);
  }

  private boolean equals(Object a, Object b) {
    if (a == null) return b == null;
    return b != null && a.equals(b);
  }

  @Nullable
  public CheckResult getResult() {
    return myResult.get();
  }

  public long getNextCheckTime() {
    return myNextCheckTime;
  }

  public long getRemoveTime() {
    return myRemoveTime;
  }

  @NotNull
  public CheckRequestMode getMode() {
    return myKey.getMode();
  }

  public boolean isExecuting() {
    return myExecuting.get();
  }

  public void setExecuting() {
    myExecuting.set(true);
  }

  @NotNull
  public PackageCheckRequest getRequest() {
    return myKey;
  }

  public void setResult(@NotNull CheckResult result) {
    updateTimes();

    myResult.set(result);
    myExecuting.set(false);
  }
}
