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

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created 08.02.13 12:00
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class FeedConstantsTest extends BaseTestCase {
  @Test
  @TestFor(issues = "TW-25512")
  public void test_does_not_accept_symbols_package() throws IOException {
    doFilterTest("a.b.c.3.4.5.nupkg", false);
    doFilterTest("a.b.c.3.4.5.symbols.nupkg", true);
  }

  private void doFilterTest(@NotNull final String name, final boolean expectedResult) throws IOException {
    Assert.assertEquals(FeedConstants.SYMBOLS_PACKAGE_FILE_NAME_FILTER.accept(name), expectedResult);
    final File tmp = new File(createTempDir(), name);
    tmp.createNewFile();
    Assert.assertEquals(FeedConstants.SYMBOLS_PACKAGE_FILE_FILTER.accept(tmp), expectedResult);
  }

}
