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

package jetbrains.buildServer.nuget.tests.server.feed.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import jetbrains.buildServer.nuget.server.feed.server.controllers.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PingBackController;
import jetbrains.buildServer.nuget.server.feed.server.impl.NuGetServerTokensImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 13:28
 */
public class PingControllerTest extends BaseTestCase {
  private Mockery m;
  private NuGetServerRunnerTokens mySettings;
  private PingBackController myController;

  private HttpServletRequest myRequest;
  private HttpServletResponse myResponse;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();

    MetadataControllersPaths paths = m.mock(MetadataControllersPaths.class);
    mySettings = new NuGetServerTokensImpl();

    myRequest = m.mock(HttpServletRequest.class);
    myResponse = m.mock(HttpServletResponse.class);

    myController = new PingBackController(paths, mySettings);

    m.checking(new Expectations(){{
      allowing(myResponse).setCharacterEncoding(with(any(String.class)));
      allowing(myResponse).setContentType(with(any(String.class)));
    }});
  }

  @Test
  public void test_ping() throws Exception {

    final StringWriter w = new StringWriter();
    m.checking(new Expectations(){{
      oneOf(myResponse).setHeader("X-TeamCity-HostId", mySettings.getServerToken());
      oneOf(myResponse).getWriter(); will(returnValue(new PrintWriter(w)));
    }});

    myController.processRequest(myRequest, myResponse);

    Assert.assertEquals(w.toString(), mySettings.getServerToken());
    m.assertIsSatisfied();
  }


}