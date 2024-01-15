

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:34
 */
public class PackageCheckEntry implements CheckablePackage {
  private final TimeService myTime;
  private final PackageCheckerSettings mySettings;
  private final PackageCheckRequest myKey;
  private final AtomicReference<CheckResult> myResult = new AtomicReference<CheckResult>();
  private final AtomicBoolean myExecuting = new AtomicBoolean();
  private final AtomicBoolean myFreshRequest = new AtomicBoolean(true);

  private volatile long myNextCheckTime;
  private volatile long myRemoveTime;

  private volatile long myCheckInterval;
  private volatile long myRemoveInterval;

  public PackageCheckEntry(@NotNull final PackageCheckRequest key,
                           @NotNull final TimeService time,
                           @NotNull final PackageCheckerSettings settings) {
    myKey = key;
    myTime = time;
    mySettings = settings;
    myCheckInterval = key.getCheckInterval();
    myRemoveInterval = removeInterval(key);

    long now = myTime.now();
    myNextCheckTime = now + myCheckInterval;
    myRemoveTime = now + myRemoveInterval;
  }

  @NotNull
  public CheckRequestMode getMode() {
    return getRequest().getMode();
  }

  @NotNull
  public SourcePackageReference getPackage() {
    return getRequest().getPackage();
  }

  private long removeInterval(@NotNull final PackageCheckRequest request) {
    return mySettings.getPackageCheckRequestIdleRemoveInterval(request.getCheckInterval());
  }

  public boolean forRequest(@NotNull final PackageCheckRequest request) {
    return equals(myKey.getMode(), request.getMode()) && myKey.getPackage().equals(request.getPackage());
  }

  public void update(@NotNull final PackageCheckRequest request) {
    if (!forRequest(request)) throw new IllegalArgumentException("Incompatible request");

    myFreshRequest.set(true);

    myCheckInterval = Math.min(myCheckInterval, request.getCheckInterval());
    myRemoveInterval = Math.max(myRemoveInterval, removeInterval(request));

    long now = myTime.now();
    myNextCheckTime = Math.min(myNextCheckTime, now + myCheckInterval);
    myRemoveTime = Math.max(myRemoveTime, now + myRemoveInterval);
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
    final long now = myTime.now();
    myNextCheckTime = now + myCheckInterval;
    //if this is the first update after requires, let's recall it longer
    if (myFreshRequest.getAndSet(false)) {
      myRemoveTime = now + myRemoveInterval;
    }

    //avoid checking errorneous requiests
    if (result.getError() != null) {
      myNextCheckTime = now + myRemoveInterval + myCheckInterval;
    }
    //should not change remove time, to give
    //a chance to remove package from the list

    myResult.set(result);
    myExecuting.set(false);
  }
}
