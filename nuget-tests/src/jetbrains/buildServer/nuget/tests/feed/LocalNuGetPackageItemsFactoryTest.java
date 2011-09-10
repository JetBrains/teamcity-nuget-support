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

    final NuGetItem aPackage = myFactory.createPackage("detailsUrl", pkg, false, false);

    final NuGetAtomItem atomItem = aPackage.getAtomItem();
    final NuGetProperties properties = aPackage.getProperties();

    Assert.assertEquals(
            serializeObject(NuGetAtomItem.class, atomItem),
            "DownloadPath = Ninject.MVC3.2.2.2.0.nupkg\n" +
            "ItemAuthors = Remo Gloor, Ian Davis\n" +
            "ItemName = Ninject.MVC3\n" +
            "ItemSummary = Extension for Ninject providing integration with ASP.NET MVC3\n" +
            "ItemTitle = Ninject.MVC3\n" +
            "ItemUpdated = Tue Sep 06 22:25:36 CEST 2011\n" +
            "ItemVersion = 2.2.2.0");
    Assert.assertEquals(
            serializeObject(NuGetProperties.class, properties),
            "Authors = Remo Gloor, Ian Davis\n" +
                    "Categories = null\n" +
                    "Copyright = null\n" +
                    "Created = Tue Sep 06 22:25:36 CEST 2011\n" +
                    "Dependencies = Ninject:[2.2.0.0, 2.3.0.0)|WebActivator:1.4\n" +
                    "Description = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "ExternalPackageUrl = detailsUrl\n" +
                    "GalleryDetailsUrl = detailsUrl\n" +
                    "IconUrl = https://github.com/ninject/ninject/raw/master/logos/Ninject-Logo32.png\n" +
                    "Id = Ninject.MVC3\n" +
                    "IsLatestVersion = false\n" +
                    "LicenseUrl = https://github.com/ninject/ninject.web.mvc/raw/master/mvc3/LICENSE.txt\n" +
                    "PackageHash = vAG563nUohsNV8gsOOARPS3RJubWWSzUQ+JRLTne4yzE7/TR/rDjD1eS9klB682FvInUP2x48OuQoIgpwKqaIA==\n" +
                    "PackageHashAlgorithm = SHA512\n" +
                    "PackageSize = 34857\n" +
                    "Prerelease = false\n" +
                    "ProjectUrl = http://www.ninject.org\n" +
                    "ReleaseNotes = null\n" +
                    "ReportAbuseUrl = detailsUrl\n" +
                    "RequireLicenseAcceptance = true\n" +
                    "Summary = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "Tags = Ninject ioc di web mvc3\n" +
                    "Title = Ninject.MVC3\n" +
                    "Version = 2.2.2.0");
  }

  private <T> String serializeObject(@NotNull final Class<T> clazz, Object t) throws InvocationTargetException, IllegalAccessException {
    Assert.assertTrue(clazz.isInstance(t));

    final Map<String, String> map = new TreeMap<String, String>();
    for (Method method : clazz.getMethods()) {
      String name = method.getName();
      if (!name.startsWith("get")) continue;
      name = name.substring(3);
      final Object invoke = method.invoke(t);
      map.put(name, invoke == null ? null : invoke.toString());
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : map.entrySet()) {
      sb.append(e.getKey()).append(" = ").append(e.getValue()).append("\n");
    }
    return sb.toString().trim();
  }
}
