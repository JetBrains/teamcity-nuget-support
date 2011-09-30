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
import jetbrains.buildServer.nuget.server.trigger.impl.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckRequestModeFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 18:04
 */
public class CheckRequestModeFactoryTest extends BaseTestCase {
  private CheckRequestModeFactory myFactory;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFactory = new CheckRequestModeFactory();
  }

  @Test
  public void testTeamCityMode() {
    Assert.assertTrue(myFactory.createTeamCityChecker().equals(myFactory.createTeamCityChecker()));

    Set<CheckRequestMode> set = new HashSet<CheckRequestMode>();
    set.add(myFactory.createTeamCityChecker());
    for(int i = 0; i < 100; i ++) {
      Assert.assertFalse(set.add(myFactory.createTeamCityChecker()));
    }
  }

  @Test
  public void testNuGetMode_eq() throws IOException {
    final File path = createTempFile();

    Assert.assertTrue(myFactory.createNuGetChecker(path).equals(myFactory.createNuGetChecker(path)));
  }

  @Test
  public void testNuGetMode_diff() throws IOException {
    Assert.assertFalse(myFactory.createNuGetChecker(createTempFile()).equals(myFactory.createNuGetChecker(createTempFile())));
  }

  @Test
  public void testModesEqual() throws IOException {
    Assert.assertFalse(myFactory.createNuGetChecker(createTempFile()).equals(myFactory.createTeamCityChecker()));
  }
}
