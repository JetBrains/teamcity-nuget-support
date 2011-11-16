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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

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

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    mySettings = m.mock(PackageCheckerSettings.class);
    myTime = m.mock(TimeService.class);
    mySystemInfo = m.mock(SystemInfo.class);
    myFactory = new CheckRequestModeFactory(mySystemInfo);

    m.checking(new Expectations(){{
      allowing(myTime).now(); will(returnValue(42L));
      allowing(mySettings).getPackageCheckRequestIdleRemoveInterval(with(any(long.class))); will(returnValue(45L));
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
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
