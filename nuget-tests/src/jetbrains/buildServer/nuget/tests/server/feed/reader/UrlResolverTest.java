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

package jetbrains.buildServer.nuget.tests.server.feed.reader;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolver;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolverImpl;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 15:26
 */
public class UrlResolverTest extends BaseTestCase {
  private Mockery m;
  private FeedClient myFeedClient;
  private UrlResolver myResolver;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myFeedClient = m.mock(FeedClient.class);
    myResolver = new UrlResolverImpl(myFeedClient, new FeedGetMethodFactory());
  }

  @Test
  public void test_should_support_200() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseStatus(200)));
    }});

    final Pair<String, HttpResponse> pair = myResolver.resolvePath("http://www.jetbrains.com");
    Assert.assertEquals(pair.first, "http://www.jetbrains.com");
  }

  @Test
  public void test_should_support_3xx() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://www.google.com")));
      oneOf(myFeedClient).execute(with(httpGet("http://www.google.com")));
      will(returnValue(responseStatus(200)));
    }});

    final Pair<String, HttpResponse> pair = myResolver.resolvePath("http://www.jetbrains.com");
    Assert.assertEquals(pair.first, "http://www.google.com");
  }

  @Test
  public void test_should_support_3xx_ms() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com/redirect?fwLink=555")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://www.google.com")));
      oneOf(myFeedClient).execute(with(httpGet("http://www.google.com")));
      will(returnValue(responseStatus(200)));
    }});

    final Pair<String, HttpResponse> pair = myResolver.resolvePath("http://www.jetbrains.com/redirect?fwLink=555");
    Assert.assertEquals(pair.first, "http://www.google.com");
  }

  @Test
  public void test_should_support_3xx_trimSlash() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com/redirect?fwLink=555")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://www.google.com///")));
      oneOf(myFeedClient).execute(with(httpGet("http://www.google.com///")));
      will(returnValue(responseStatus(200)));
    }});

    final Pair<String, HttpResponse> pair = myResolver.resolvePath("http://www.jetbrains.com/redirect?fwLink=555");
    Assert.assertEquals(pair.first, "http://www.google.com");
  }

  @Test
  public void test_should_support_3xx_multi() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://domain_1.jonnyzzz.com")));

      oneOf(myFeedClient).execute(with(httpGet("http://domain_1.jonnyzzz.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_TEMPORARILY, "http://domain_2.jonnyzzz.com")));

      oneOf(myFeedClient).execute(with(httpGet("http://domain_2.jonnyzzz.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_TEMPORARY_REDIRECT, "http://domain_3.jonnyzzz.com")));

      oneOf(myFeedClient).execute(with(httpGet("http://domain_3.jonnyzzz.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://domain_4.jonnyzzz.com")));

      oneOf(myFeedClient).execute(with(httpGet("http://domain_4.jonnyzzz.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://www.google.com")));

      oneOf(myFeedClient).execute(with(httpGet("http://www.google.com")));
      will(returnValue(responseStatus(200)));
    }});

    final Pair<String, HttpResponse> pair = myResolver.resolvePath("http://www.jetbrains.com");
    Assert.assertEquals(pair.first, "http://www.google.com");
  }

  @Test
  public void test_should_support_3xx_loop() throws IOException {
    m.checking(new Expectations() {{
      allowing(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseLocationStatus(HttpStatus.SC_MOVED_PERMANENTLY, "http://www.jetbrains.com")));
    }});

    try {
      myResolver.resolvePath("http://www.jetbrains.com");
    } catch (IOException e) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void test_should_fail_on_500() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseStatus(500)));
    }});

    try {
      myResolver.resolvePath("http://www.jetbrains.com");
    } catch (IOException e) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void test_should_fail_on_400() throws IOException {
    m.checking(new Expectations() {{
      oneOf(myFeedClient).execute(with(httpGet("http://www.jetbrains.com")));
      will(returnValue(responseStatus(400)));
    }});

    try {
      myResolver.resolvePath("http://www.jetbrains.com");
    } catch (IOException e) {
      return;
    }
    Assert.fail();
  }

  private static HttpResponse responseStatus(int status) {
    return new BasicHttpResponse(HttpVersion.HTTP_1_0, status, "Status: " + status);
  }

  private static HttpResponse responseLocationStatus(int status, @NotNull String location) {
    final BasicHttpResponse res = new BasicHttpResponse(HttpVersion.HTTP_1_0, status, "Status: " + status);
    res.addHeader("Location", location);
    return res;
  }

  private static BaseMatcher<HttpUriRequest> httpGet(@NotNull final String url) {
    return new BaseMatcher<HttpUriRequest>() {
      public boolean matches(Object o) {
        if (!(o instanceof org.apache.http.client.methods.HttpGet)) return false;
        org.apache.http.client.methods.HttpGet get = (org.apache.http.client.methods.HttpGet) o;
        return get.getURI().toString().equals(url);
      }

      public void describeTo(Description description) {
        description.appendText("HttpGet to ").appendText(url);
      }
    };
  }
}
