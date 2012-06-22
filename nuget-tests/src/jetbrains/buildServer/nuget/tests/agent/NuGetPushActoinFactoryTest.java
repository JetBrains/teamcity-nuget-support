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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:46
 */
public class NuGetPushActoinFactoryTest extends NuGetActionFactoryTestCase {
  protected NuGetPublishParameters ps;
  protected File myFile;
  protected File myNuGet;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ps = m.mock(NuGetPublishParameters.class);
    myFile = createTempFile();
    myNuGet = createTempFile();
  }

  private org.hamcrest.Matcher<List<String>> arguments(final String... args) {
    return new BaseMatcher<List<String>>() {
      public boolean matches(Object o) {
        List<String> text = (List<String>) o;

        if (text.size() != args.length) {
          return false;
        }

        for (int i = 0; i < args.length; i++) {
          if (args[i].startsWith("%%teamcity_nuget_api_key_")) {
            final String actual = text.get(i).replaceAll("%%teamcity_nuget_api_key_\\d+%%", "%%teamcity_nuget_api_key_DDD%%");
            if (!actual.equals(args[i])) return false;
          }
        }
        return true;
      }

      public void describeTo(Description description) {
        description.appendText("Should match: [");
        for (String arg : args) {
          description.appendValue(arg);
          description.appendText(", ");
        }
        description.appendText("]");
      }
    };
  }

  private org.hamcrest.Matcher<Map<String, String>> envApi(@NotNull final String key) {
    return new BaseMatcher<Map<String, String>>() {
      public boolean matches(Object o) {
        Map<String, String> map = (Map<String, String>) o;
        for (Map.Entry<String, String> e : map.entrySet()) {
          if (e.getKey().startsWith("teamcity_nuget_api_key_")) {
            return key.equals(e.getValue());
          }
        }
        return false;
      }

      public void describeTo(Description description) {
        description.appendText("Environment map should contain value: ").appendValue(key);
      }
    };
  }

  @Test
  public void test_command_push() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue(new PackageSourceImpl("push-feed")));
      allowing(ps).getCreateOnly(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(with(equal(ctx)), with(equal(myNuGet.getPath())),
              with(arguments("push", myFile.getPath(), "%%teamcity_nuget_api_key_DDD%%", "-Source", "push-feed")),
              with(equal(myFile.getParentFile())),
              with(envApi("api-key-guid"))
      );
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

  @Test
  public void test_command_push_no_source() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue(null));
      allowing(ps).getCreateOnly(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(with(equal(ctx)), with(equal(myNuGet.getPath())),
              with(arguments("push", myFile.getPath(), "%%teamcity_nuget_api_key_DDD%%")),
              with(equal(myFile.getParentFile())),
              with(envApi("api-key-guid"))
      );
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

  @Test
  public void test_command_push_no_pacakge() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue(new PackageSourceImpl("push-feed")));
      allowing(ps).getCreateOnly(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(with(equal(ctx)), with(equal(myNuGet.getPath())),
              with(arguments("push", myFile.getPath(), "%%teamcity_nuget_api_key_DDD%%", "-CreateOnly", "-Source", "push-feed")),
              with(equal(myFile.getParentFile())),
              with(envApi("api-key-guid"))
      );
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

}
