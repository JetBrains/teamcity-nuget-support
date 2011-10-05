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
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:17
 */
public class PackageChangesManagerTest extends BaseTestCase implements TimeService {
  private long myTime = 1;
  private Mockery m;
  private PackageChangesManagerImpl myManager;
  private Map<String, CheckRequestMode> myModes = new HashMap<String, CheckRequestMode>();
  private PackageCheckRequestFactory myFactory;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    final PackageCheckerSettings settings = new PackageCheckerSettingsImpl();
    myFactory = new PackageCheckRequestFactory(settings);
    myManager = new PackageChangesManagerImpl(this, settings);
  }

  private void advanceTime(long delta) {
    myTime += delta;
  }

  public long now() {
    return myTime;
  }


  @Test
  public void test_empty() {
    Assert.assertTrue(myManager.getItemsToCheckNow().isEmpty());
    Assert.assertEquals(myManager.getSleepTime(), 450000);
    myManager.cleaupObsolete();
  }

  @Test
  public void test_oneMode() {
    final PackageCheckRequest a = req("a");
    a.setCheckInterval(10 * 60 * 1000);
    Assert.assertNull(myManager.checkPackage(a));

    Assert.assertTrue(myManager.getItemsToCheckNow().isEmpty());
    myManager.cleaupObsolete();

    advanceTime(10 * 60 * 1001);

    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 1);

    advanceTime(100L * 10 * 60 * 1001);

    myManager.cleaupObsolete();
    Assert.assertTrue(myManager.getItemsToCheckNow().isEmpty());
  }

  @Test
  public void test_sameRequestsWithDifferentTimes() {
    final PackageCheckRequest a = req("a");
    final PackageCheckRequest b = req("a");
    final PackageCheckRequest c = req("a");

    a.setCheckInterval(1000 * 1000);
    b.setCheckInterval(5000 * 1000);
    c.setCheckInterval(9000 * 1000);

    checkAll(b,c,a);

    Assert.assertTrue(myManager.getItemsToCheckNow().isEmpty());

    advanceTime(1000 * 1000 + 1);
    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 1);

    advanceTime(8000 * 1000 + 1);
    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 1);

    final PackageCheckEntry next = myManager.getItemsToCheckNow().iterator().next();
    setResult(next);

    advanceTime(3000 * 1000);
    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 1);
  }

  private void setResult(PackageCheckEntry next) {
    next.setResult(CheckResult.succeeded(Collections.<SourcePackageInfo>emptyList()));
  }


  @Test
  public void test_should_remove_old_task() {
    final PackageCheckRequest a = req("a");
    a.setCheckInterval(1000 * 1000);

    checkAll(a);
    advanceTime(1000 * 1000 + 1);

    int totalTimes = 10;
    while(true) {
      Assert.assertTrue(totalTimes-- > 0, "Task must be removed as absolete");

      final Collection<PackageCheckEntry> items = myManager.getItemsToCheckNow();
      if (items.isEmpty()) return;

      final PackageCheckEntry next = items.iterator().next();
      setResult(next);

      myManager.cleaupObsolete();
      advanceTime(1000 * 1000 + 1);
    }
  }

  @Test
  public void test_should_update_remove_time() {
    final PackageCheckRequest a = req("a");
    final PackageCheckRequest b = req("a");
    a.setCheckInterval(1000 * 1000);
    b.setCheckInterval(10000 * 1000);

    checkAll(a, b);
    advanceTime(1000 * 1000 + 1);

    for(int totalTimes = 10; totalTimes-- > 0; ) {
      final Collection<PackageCheckEntry> items = myManager.getItemsToCheckNow();
      Assert.assertFalse(items.isEmpty());

      final PackageCheckEntry next = items.iterator().next();
      setResult(next);

      myManager.cleaupObsolete();
      advanceTime(1000 * 1000 + 1);
    }

    advanceTime(11 * 10000 * 1000);
    myManager.cleaupObsolete();
    Assert.assertTrue(myManager.getItemsToCheckNow().isEmpty());
  }

  @Test
  public void test_group_by_mode() {
    final PackageCheckRequest a1 = req("a");
    final PackageCheckRequest a2 = req("a");
    final PackageCheckRequest a3 = req("a");

    final PackageCheckRequest b1 = req("b");
    final PackageCheckRequest b2 = req("b");
    final PackageCheckRequest b3 = req("b");

    checkAll(a1, a2, a3, b1, b2, b3);
    advanceTime(10000);

    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 2);
  }

  @Test
  public void test_merge_same() {
    final PackageCheckRequest a1 = req("a");
    final PackageCheckRequest a2 = req("a");
    final PackageCheckRequest a3 = req("a");

    checkAll(a1, a2, a3);
    advanceTime(10000);

    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 1);
  }

  @Test
  public void test_merge_different_sources() {
    final PackageCheckRequest a1 = reqS("a", "oo");
    final PackageCheckRequest a2 = reqS("a", null);
    final PackageCheckRequest a3 = reqS("a", "qq");

    checkAll(a1, a2, a3);
    advanceTime(10000);

    Assert.assertEquals(myManager.getItemsToCheckNow().size(), 3);
  }


  private void checkAll(PackageCheckRequest... req) {
    for (PackageCheckRequest r : req) {
      myManager.checkPackage(r);
    }
  }


  private PackageCheckRequest req(String mode) {
    final PackageCheckRequest id = myFactory.createRequest(createMode(mode), null, "id", null);
    id.setCheckInterval(1);
    return id;
  }

  private PackageCheckRequest reqS(String mode, @Nullable String source) {
    final PackageCheckRequest id = myFactory.createRequest(createMode(mode), source, "id", null);
    id.setCheckInterval(1);
    return id;
  }


  @NotNull
  private CheckRequestMode createMode(@NotNull final String id) {
    if (myModes.containsKey(id)) return myModes.get(id);

    final CheckRequestMode checkRequestMode = new CheckRequestMode() {
      @Override
      public String toString() {
        return "Mode: " + id;
      }
    };
    myModes.put(id, checkRequestMode);
    return checkRequestMode;
  }

}
