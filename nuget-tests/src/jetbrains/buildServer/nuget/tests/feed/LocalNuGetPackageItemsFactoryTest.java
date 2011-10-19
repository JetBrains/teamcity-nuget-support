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
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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

  @NotNull
  private String store(@NotNull Map<String, String> map) {
    map = new TreeMap<String, String>(map);
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
    }
    return sb.toString().trim();
  }

  @Test
  public void test_NinjectMVC() throws InvocationTargetException, IllegalAccessException, PackageLoadException {
    final File pkg = Paths.getTestDataPath("packages/Ninject.MVC3.2.2.2.0.nupkg");
    Assert.assertTrue(pkg.isFile());

    final Map<String, String> aPackage = myFactory.loadPackage(pkg);
    Assert.assertEquals(
            store(aPackage),
            "Authors = Remo Gloor, Ian Davis\n" +
                    "Dependencies = Ninject:[2.2.0.0, 2.3.0.0)|WebActivator:1.4\n" +
                    "Description = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "IconUrl = https://github.com/ninject/ninject/raw/master/logos/Ninject-Logo32.png\n" +
                    "Id = Ninject.MVC3\n" +
                    "LicenseUrl = https://github.com/ninject/ninject.web.mvc/raw/master/mvc3/LICENSE.txt\n" +
                    "PackageHash = vAG563nUohsNV8gsOOARPS3RJubWWSzUQ+JRLTne4yzE7/TR/rDjD1eS9klB682FvInUP2x48OuQoIgpwKqaIA==\n" +
                    "PackageHashAlgorithm = SHA512\n" +
                    "PackageSize = 34857\n" +
                    "ProjectUrl = http://www.ninject.org\n" +
                    "RequireLicenseAcceptance = true\n" +
                    "Summary = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "Tags = Ninject ioc di web mvc3\n" +
                    "Title = Ninject.MVC3\n" +
                    "Updated = 2011-10-19T11:10:17Z\n" +
                    "Version = 2.2.2.0");
  }

}
