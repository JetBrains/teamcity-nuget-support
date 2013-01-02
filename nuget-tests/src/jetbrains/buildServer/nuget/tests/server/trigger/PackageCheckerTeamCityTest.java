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

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.feed.FeedCredentials;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.checker.PackageCheckerTeamCity;
import jetbrains.buildServer.util.TestFor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 13.08.12 15:30
 */
public class PackageCheckerTeamCityTest extends PackageCheckerTestBase<PackageCheckerTeamCity> {
  @Override
  protected PackageCheckerTeamCity createChecker() {
    return new PackageCheckerTeamCity(myReader);
  }

  @Test
  public void test_available_01() throws IOException {
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));
  }

  @Test
  public void test_available_02() throws IOException {
    Assert.assertTrue(myChecker.accept(new PackageCheckRequest(javaMode(), ref())));
  }

  @Test
  public void test_available_accept_file() throws IOException {
    final PackageCheckRequest req = new PackageCheckRequest(javaMode(), new SourcePackageReference(createTempDir().getPath(), "foo.bar", null));
    Assert.assertTrue(myChecker.accept(req));
  }

  @Test
  public void test_file() throws IOException {
    final SourcePackageReference ref = new SourcePackageReference(createTempDir().getPath(), "foo.bar", null);

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(javaMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(failed("HTTP")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_file2() throws IOException {
    final SourcePackageReference ref = new SourcePackageReference("file:///" + createTempDir().getPath(), "foo.bar", null);

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(javaMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(failed("HTTP")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_http() throws IOException {
    final SourcePackageReference ref = new SourcePackageReference("http://foo.bar", "foo.bar", null);

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(javaMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(empty()));

      oneOf(myReader).queryPackageVersions("http://foo.bar", null, "foo.bar"); will(returnValue(Collections.emptyList()));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-23193")
  public void test_http_failed() throws IOException {
    final SourcePackageReference ref = new SourcePackageReference("http://foo.bar", "foo.bar", null);

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(javaMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(failed("foo.bar", "Failed. Error")));

      oneOf(myReader).queryPackageVersions("http://foo.bar", null, "foo.bar"); will(throwException(new IOException("Failed. Error")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-20764")
  public void test_http_auth_supported() throws Throwable {
    final SourcePackageReference ref = new SourcePackageReference(
            "http://foo.bar",
            "username",
            "password",
            "foo.bar",
            null,
            false);

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations() {{
      allowing(task).getPackage();
      will(returnValue(ref));
      allowing(task).getMode();
      will(returnValue(javaMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(failed("foo.bar", "Failed. Error")));

      oneOf(myReader).queryPackageVersions("http://foo.bar", new FeedCredentials("username", "password"), "foo.bar");
      will(throwException(new IOException("Failed. Error")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));
  }

}
