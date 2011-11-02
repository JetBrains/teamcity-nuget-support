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
import jetbrains.buildServer.nuget.server.feed.server.controllers.*;
import jetbrains.buildServer.nuget.server.feed.server.impl.NuGetServerTokensImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 12:20
 */
public class MetadataControllerBaseTest extends BaseTestCase {
  private Mockery m;
  private NuGetServerRunnerTokens myTokens;
  private MetadataControllerHandler myHandler;
  private MetadataControllerBase myController;

  private HttpServletRequest myRequest;
  private HttpServletResponse myResponse;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();

    MetadataControllersPaths paths = m.mock(MetadataControllersPaths.class);
    myTokens = new NuGetServerTokensImpl();
    myHandler = m.mock(MetadataControllerHandler.class);

    myRequest = m.mock(HttpServletRequest.class);
    myResponse = m.mock(HttpServletResponse.class);

    myController = new MetadataControllerBase(myTokens, myHandler);
  }

  @Test
  public void test_reject_token() throws Exception {

    m.checking(new Expectations(){{
      allowing(myRequest).getHeader(with(any(String.class))); will(returnValue(null));

      oneOf(myResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }});

    myController.doHandle(myRequest, myResponse);

    m.assertIsSatisfied();
  }

  @Test
  public void test_reject_token2() throws Exception {

    m.checking(new Expectations(){{

      allowing(myRequest).getHeader(with(any(String.class))); will(returnValue(null));
      allowing(myRequest).getHeader("X-TeamCity-HostId"); will(returnValue("unknown"));
      oneOf(myResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }});

    myController.doHandle(myRequest, myResponse);

    m.assertIsSatisfied();
  }

  @Test
  public void test_accept_token() throws Exception {

    m.checking(new Expectations(){{

      allowing(myRequest).getHeader("X-TeamCity-HostId"); will(returnValue(myTokens.getAccessToken()));
      allowing(myRequest).getHeader(with(any(String.class))); will(returnValue(null));

      oneOf(myHandler).processRequest(myRequest, myResponse);
    }});

    myController.doHandle(myRequest, myResponse);

    m.assertIsSatisfied();
  }
}
