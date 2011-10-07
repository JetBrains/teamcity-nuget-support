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
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.server.NuGetFeedException;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRegister;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerUri;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.ExceptionUtil;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 20:08
 */
public class NuGetServerRegisterTest extends BaseTestCase {
  private Mockery m;
  private FeedClient myClient;
  private NuGetServerUri myUris;
  private ToolPaths myPaths;
  private SecurityContextEx mySecurityContext;
  private SBuild myBuild;
  private BuildArtifacts myBuildArtifacts;
  private BuildArtifact myArtifacts;
  private NuGetServerRegister myReg;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myClient = m.mock(FeedClient.class);
    myUris = m.mock(NuGetServerUri.class);
    myPaths = m.mock(ToolPaths.class);
    mySecurityContext = m.mock(SecurityContextEx.class);
    myBuild = m.mock(SBuild.class);
    myBuildArtifacts = m.mock(BuildArtifacts.class);
    myArtifacts = m.mock(BuildArtifact.class);
    myReg = new NuGetServerRegister(myClient, myUris, myPaths, mySecurityContext, new FeedGetMethodFactory());

    final File temp = createTempDir();
    m.checking(new Expectations(){{
      try {
        allowing(mySecurityContext).runAsSystem(with(any(SecurityContextEx.RunAsAction.class)));
      } catch (Throwable throwable) {
        ExceptionUtil.rethrowAsRuntimeException(throwable);
      }
      will(new CustomAction("Execute") {
        public Object invoke(Invocation invocation) throws Throwable {
          ((SecurityContextEx.RunAsAction)invocation.getParameter(0)).run();
          return null;
        }
      });
      allowing(myBuild).getArtifacts(BuildArtifactsViewMode.VIEW_ALL); will(returnValue(myBuildArtifacts));
      allowing(myBuild).getBuildId(); will(returnValue(555L));
      allowing(myBuild).getBuildTypeId(); will(returnValue("btX"));

      allowing(myPaths).getArtifactsDirectory(); will(returnValue(temp));
      allowing(myBuild).getArtifactsDirectory(); will(returnValue(new File(temp, "a/b/c")));

    }});
  }

  @Test
  public void testCallRemote() throws NuGetFeedException, IOException {
    final String path = "aaa/super.1.2.3.4.nupkg";

    final HttpResponse response = m.mock(HttpResponse.class);
    final org.apache.http.StatusLine statusLine = m.mock(org.apache.http.StatusLine.class);

    m.checking(new Expectations(){{
      allowing(myBuildArtifacts).getArtifact(path); will(returnValue(myArtifacts));
      allowing(myArtifacts).getRelativePath(); will(returnValue(path));

      allowing(myUris).getAddPackageUri(); will(returnValue("http://localhost:9999/nuget4/add"));

      oneOf(myClient).execute(with(new BaseMatcher<HttpUriRequest>(){
        public boolean matches(Object o) {
          HttpGet get = (HttpGet) o;
          final RequestLine requestLine = get.getRequestLine();
          final String uri = requestLine.getUri();
          System.out.println("uri = " + uri);
          return "http://localhost:9999/nuget4/add?buildType=btX&buildId=555&downloadUrl=%2Frepository%2Fdownload%2FbtX%2F555%3Aid%2Faaa%2Fsuper.1.2.3.4.nupkg&packageFile=a%5Cb%5Cc%5Caaa%5Csuper.1.2.3.4.nupkg".equals(uri);
        }

        public void describeTo(Description description) {

        }
      }));
      will(returnValue(response));

      allowing(response).getStatusLine(); will(returnValue( statusLine));
      allowing(statusLine).getStatusCode(); will(returnValue(200));
    }});


    myReg.registerPackage(myBuild, path);
  }



}
