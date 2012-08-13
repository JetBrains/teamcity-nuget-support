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

import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageChecker;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerSettings;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;

import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 21:07
 */
public abstract class PackageCheckerTestBase<T extends PackageChecker> extends TriggerTestBase {
  protected NuGetFeedReader myReader;
  protected ListPackagesCommand myCommand;
  protected PackageCheckerSettings mySettings;
  protected ExecutorService myExecutor;

  protected T myChecker;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myCommand = m.mock(ListPackagesCommand.class);
    mySettings = m.mock(PackageCheckerSettings.class);
    myExecutor = m.mock(ExecutorService.class);
    myReader = m.mock(NuGetFeedReader.class);
    myChecker = createChecker();

    m.checking(new Expectations(){{
      allowing(myExecutor).submit(with(any(Runnable.class))); will(new CustomAction("Execute task in same thread") {
        public Object invoke(Invocation invocation) throws Throwable {
          Runnable action = (Runnable) invocation.getParameter(0);
          action.run();
          return null;
        }
      });
    }});
  }

  protected abstract T createChecker();
}
