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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.11.11 13:23
 */
public class PackageCheckEntryTest extends BaseTestCase {
  private Mockery m;
  private PackageCheckerSettings mySettings;
  private TimeService myTime;
  private SystemInfo mySystemInfo;
  private CheckRequestModeFactory myFactory;
  private long myNow;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    mySettings = m.mock(PackageCheckerSettings.class);
    myTime = m.mock(TimeService.class);
    mySystemInfo = m.mock(SystemInfo.class);
    myFactory = new CheckRequestModeFactory(mySystemInfo);
    myNow = 42;

    m.checking(new Expectations(){{
      allowing(myTime).now(); will(new CustomAction("return myNow") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myNow;
        }
      });
      allowing(mySettings).getPackageCheckRequestIdleRemoveInterval(with(any(long.class))); will(returnValue(45L));
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(true));
    }});
  }

  @Test
  public void testForRequest_same() {
    assertAccept(true, request("TC", "source", "package", "version"), request("TC", "source", "package", "version"));
  }

  @Test
  public void testDoNotAcceptDifferentSource() {
    assertAccept(false,
            request("TC", "source2", "package", "version"),
            request("TC", "source1", "package", "version"));
  }

  @Test
  public void testDoNotAcceptDifferentSourceVsDefaultSource() {
    assertAccept(false,
            request("TC", null, "package", "version"),
            request("TC", "source1", "package", "version"));
  }

  @Test
  public void testDoNotAcceptDifferentVersions() {
    assertAccept(false,
            request("TC", null, "package", "version"),
            request("TC", null, "package", "version2"));
  }

  @Test
  public void testDoNotAcceptDifferentVersions2() {
    assertAccept(false,
            request("TC", null, "package", null),
            request("TC", null, "package", "version2"));
  }

  @Test
  public void testDoNotAcceptDifferentMode() {
    assertAccept(false,
            request("TC", null, "package", null),
            request("zzz", null, "package", null));
  }

  @Test
  public void testDoNotAcceptDifferentMode2() {
    assertAccept(false,
            request("qqq", null, "package", null),
            request("zzz", null, "package", null));
  }

  @Test
  public void testDoNotAcceptDifferentPackage() {
    assertAccept(false,
            request("zzz", null, "package1", null),
            request("zzz", null, "package2", null));
  }

  @Test
  public void testUpdatesRemoveDateOnSetResult() {
    PackageCheckEntry e = new PackageCheckEntry(request("WWWW", "asda", "asrwerw", null), myTime, mySettings);

    final long r1 = e.getRemoveTime();
    final long c1 = e.getNextCheckTime();
    myNow += 11123L;
    e.setResult(CheckResult.failed("asdasd"));
    Assert.assertTrue(r1 != e.getRemoveTime(), "data update should increment request life");
    Assert.assertTrue(c1 != e.getNextCheckTime(), "next check time should be updated");
  }

  @Test
  public void testUpdatesRemoveDateOnSetResult2_fail() {
    PackageCheckEntry e = new PackageCheckEntry(request("WWWW", "asda", "asrwerw", null), myTime, mySettings);
    e.setResult(CheckResult.failed("asdasd"));
    final long r1 = e.getRemoveTime();
    final long c1 = e.getNextCheckTime();
    myNow += 11123L;
    e.setResult(CheckResult.failed("asdasd"));

    Assert.assertTrue(r1 == e.getRemoveTime(), "data update should increment request life");
    Assert.assertTrue(c1 != e.getNextCheckTime(), "next check time should be updated");
    Assert.assertTrue(e.getRemoveTime() < e.getNextCheckTime(), "should not check errors before remove");
  }

  @Test
  public void testUpdatesRemoveDateOnSetResult2_success() {
    PackageCheckEntry e = new PackageCheckEntry(request("WWWW", "asda", "asrwerw", null), myTime, mySettings);
    e.setResult(CheckResult.failed("asdasd"));
    final long r1 = e.getRemoveTime();
    final long c1 = e.getNextCheckTime();
    myNow += 11123L;
    e.setResult(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList()));

    Assert.assertTrue(r1 == e.getRemoveTime(), "data update should increment request life");
    Assert.assertTrue(c1 != e.getNextCheckTime(), "next check time should be updated");
  }

  @Test
  public void testUpdatesRemoveDateOnSetResult3() {
    final PackageCheckRequest r = request("WWWW", "asda", "asrwerw", null);
    PackageCheckEntry e = new PackageCheckEntry(r, myTime, mySettings);
    e.setResult(CheckResult.failed("asdasd"));
    e.setResult(CheckResult.failed("asdasd"));
    e.update(r);

    final long r1 = e.getRemoveTime();
    final long c1 = e.getNextCheckTime();
    myNow += 11123L;
    e.setResult(CheckResult.failed("asdasd"));

    Assert.assertTrue(r1 != e.getRemoveTime(), "data update should increment request life");
    Assert.assertTrue(c1 != e.getNextCheckTime(), "next check time should be updated");
  }

  @Test
  public void testUpdatesRemoveDateOnSetResult4() {
    final PackageCheckRequest r = request("WWWW", "asda", "asrwerw", null);
    PackageCheckEntry e = new PackageCheckEntry(r, myTime, mySettings);
    e.setResult(CheckResult.failed("asdasd"));
    e.setResult(CheckResult.failed("asdasd"));
    e.update(r);
    e.setResult(CheckResult.failed("asdasd"));
    myNow += 11123L;
    final long r1 = e.getRemoveTime();
    final long c1 = e.getNextCheckTime();
    e.setResult(CheckResult.failed("asdasd"));

    Assert.assertTrue(r1 == e.getRemoveTime(), "data update should increment request life");
    Assert.assertTrue(c1 != e.getNextCheckTime(), "next check time should be updated");
  }

  @Test
  public void testUpdatesRemoveDateOnUpdateRequest() {
    final PackageCheckRequest r = request("WWWW", "asda", "asrwerw", null);
    PackageCheckEntry e = new PackageCheckEntry(r, myTime, mySettings);

    final long r1 = e.getRemoveTime();
    myNow += 11123L;
    e.update(r);
    Assert.assertTrue(r1 != e.getRemoveTime(), "data update should increment request life");
  }

  private void assertAccept(final boolean accept,
                            @NotNull final PackageCheckRequest init,
                            @NotNull final PackageCheckRequest req) {
    PackageCheckEntry e = new PackageCheckEntry(init, myTime, mySettings);
    if (accept) {
      Assert.assertTrue(e.forRequest(req));
    } else {
      Assert.assertFalse(e.forRequest(req));
    }
  }

  @NotNull
  private PackageCheckRequest request(@NotNull final String mode,
                                      @Nullable final String source,
                                      @NotNull final String aPackage,
                                      @Nullable final String version) {
    final CheckRequestMode checkMode;
    if (mode.equals("TC")) {
      checkMode = myFactory.createTeamCityChecker();
    } else {
      checkMode = myFactory.createNuGetChecker(new File(mode));
    }
    return new PackageCheckRequest(checkMode, new SourcePackageReference(source, aPackage, version));
  }

}
