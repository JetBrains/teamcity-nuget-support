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

package jetbrains.buildServer.nuget.tests.feed;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.render.NuGetAtomItem;
import jetbrains.buildServer.nuget.server.feed.render.NuGetItem;
import jetbrains.buildServer.nuget.server.feed.render.NuGetProperties;
import jetbrains.buildServer.nuget.server.feed.render.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.render.impl.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 22:11
 */
public class LocalNuGetPackageItemsFactoryTest extends BaseTestCase {
  private LocalNuGetPackageItemsFactory myFactory;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFactory = new LocalNuGetPackageItemsFactory();
  }

  @Test
  public void test_NinjectMVC() throws InvocationTargetException, IllegalAccessException, PackageLoadException {
    final File pkg = Paths.getTestDataPath("packages/Ninject.MVC3.2.2.2.0.nupkg");
    Assert.assertTrue(pkg.isFile());

    final NuGetItem aPackage = myFactory.createPackage("detailsUrl", pkg);

    final NuGetAtomItem atomItem = aPackage.getAtomItem();
    final NuGetProperties properties = aPackage.getProperties();

    Assert.assertEquals(serializeObject(NuGetAtomItem.class, atomItem), "");
    Assert.assertEquals(serializeObject(NuGetProperties.class, properties), "");
  }

  private <T> String serializeObject(@NotNull final Class<T> clazz, Object t) throws InvocationTargetException, IllegalAccessException {
    Assert.assertTrue(clazz.isInstance(t));

    final Map<String, String> map = new TreeMap<String, String>();
    for (Method method : clazz.getMethods()) {
      String name = method.getName();
      if (!name.startsWith("get")) continue;
      name = name.substring(3);
      map.put(name, method.invoke(t).toString());
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : map.entrySet()) {
      sb.append(e.getKey()).append(" = ").append(e.getValue()).append("\n");
    }
    return sb.toString();
  }
}
