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

package jetbrains.buildServer.nuget.tests.server.feed.reader;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import org.apache.http.client.methods.HttpGet;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 15:19
 */
public class FeedGetMethodFactoryTest extends BaseTestCase {
  private FeedGetMethodFactory myFactory;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFactory = new FeedGetMethodFactory();
  }

  @Test
  public void test_emptyParams() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com");
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com");
  }

  @Test
  public void test_withParams() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com?aaa=bbb");
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com?aaa=bbb");
  }

  @Test
  public void test_withParams2() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com?aaa=bbb", new Param("qqq","ppp"));
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com?aaa=bbb&qqq=ppp");
  }

  @Test
  public void test_withParams3() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com?aaa=bbb", new Param("qqq","ppp"), new Param("www","ttt"));
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com?aaa=bbb&qqq=ppp&www=ttt");
  }

  @Test
  public void test_withParams4() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com", new Param("qqq","ppp"), new Param("www","ttt"));
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com?qqq=ppp&www=ttt");
  }

  @Test
  public void test_withParams5() {
    final HttpGet get = myFactory.createGet("http://jetbrais.com", new Param("$qqq","ppp"), new Param("$www","t t 't'"));
    Assert.assertEquals(get.getURI().toString(), "http://jetbrais.com?%24qqq=ppp&%24www=t+t+%27t%27");
  }
}
