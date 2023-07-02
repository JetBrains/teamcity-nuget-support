/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.google.common.collect.Lists;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.common.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.common.index.NuGetPackageStructureVisitor;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static jetbrains.buildServer.nuget.tests.util.TCJMockUtils.createInstance;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 22:11
 */
public class LocalNuGetPackageItemsFactoryTest extends BaseTestCase {
  private Mockery m;
  private Set<InputStream> myStreams;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = createInstance();
    myStreams = new HashSet<InputStream>();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }
  }

  @NotNull
  private BuildArtifact artifact(@NotNull final File file) throws IOException {
    final BuildArtifact a = m.mock(BuildArtifact.class, file.getPath());
    m.checking(new Expectations(){{
      allowing(a).getInputStream(); will(new CustomAction("open file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final FileInputStream stream = new FileInputStream(file);
          myStreams.add(stream);
          return stream;
        }
      });
      allowing(a).getTimestamp(); will(returnValue(file.lastModified()));
      allowing(a).getSize(); will(returnValue(file.length()));
      allowing(a).getRelativePath(); will(returnValue(file.getPath()));
      allowing(a).getName(); will(returnValue(file.getName()));
    }});
    return a;
  }

  @NotNull
  private String store(@NotNull Map<String, String> map) {
    map = new TreeMap<String, String>(map);
    map.remove("Updated");
    if (map.containsKey("LastUpdated")) {
      map.put("LastUpdated", "ddd");
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
    }
    return sb.toString().trim();
  }

  @Test
  public void test_NinjectMVC() throws InvocationTargetException, IllegalAccessException, PackageLoadException, IOException {
    final File pkg = Paths.getTestDataPath("packages/Ninject.MVC3.2.2.2.0.nupkg");
    Assert.assertTrue(pkg.isFile());
    Assert.assertEquals(
            store(loadPackage(pkg)),
            "Authors = Remo Gloor, Ian Davis\n" +
                    "Dependencies = Ninject:[2.2.0.0, 2.3.0.0)|WebActivator:1.4\n" +
                    "Description = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "IconUrl = https://github.com/ninject/ninject/raw/master/logos/Ninject-Logo32.png\n" +
                    "Id = Ninject.MVC3\n" +
                    "IsPrerelease = false\n" +
                    "Language = en-US\n" +
                    "LicenseUrl = https://github.com/ninject/ninject.web.mvc/raw/master/mvc3/LICENSE.txt\n" +
                    "NormalizedVersion = 2.2.2\n" +
                    "ProjectUrl = http://www.ninject.org\n" +
                    "RequireLicenseAcceptance = true\n" +
                    "Summary = Extension for Ninject providing integration with ASP.NET MVC3\n" +
                    "Tags = Ninject ioc di web mvc3\n" +
                    "Version = 2.2.2.0");
  }

  @Test
  @TestFor(issues = "TW-21975")
  public void test_dependencies_20() throws InvocationTargetException, IllegalAccessException, PackageLoadException, IOException {
    final File pkg = Paths.getTestDataPath("packages/PackageWithPlatformDependencies.3.0.0.nupkg");
    Assert.assertTrue(pkg.isFile());
    final String dependencies = NuGetUtils.getValue(loadPackage(pkg), "Dependencies");
    Assert.assertNotNull(dependencies);
    Assert.assertEquals(dependencies, "Ninject:[2.2.0.0, 2.3.0.0)|WebActivator:1.4|jQuery:1.2.4:net40|WebActivator:1.3.4:net40|RouteMagic:1.1.0|Microsoft.Net.Http:2.0.20710.0:net40-client|Endjeeeeore:3.0.0.0:net40-client|Endjtttttmposition:3.0.0.0:net40-client|Entttre:3.0.0.0:net40-full|Endjittttosition:3.0.0.0:net40-full|Endeeeeore:3.0.0.0:Net45|Endjttre:3.0.0.0:portable-windows8+net45|EnqqqweCore:3.0.0.0:WinRT45|Endjbcvbcvore:3.0.0.0:WP8");
  }

  @Test
  @TestFor(issues = "TW-43254")
  public void test_longDependenciesList() throws IOException, PackageLoadException {
    final File pkg = Paths.getTestDataPath("packages/LongDependenciesList.1.2.3.nupkg");
    final Map<String, String> attributes = loadPackage(pkg);
    final List<String> dependencies = CollectionsUtil.filterCollection(attributes.keySet(), new Filter<String>() {
      @Override
      public boolean accept(@NotNull String key) {
        return key.startsWith("Dependencies");
      }
    });
    Assert.assertEquals(dependencies.size(), 4);
  }

  @Test
  @TestFor(issues = "TW-26658")
  public void test_title() throws InvocationTargetException, IllegalAccessException, PackageLoadException, IOException {
    final File pkg = Paths.getTestDataPath("packages/YCM.Web.UI.1.0.20.7275.nupkg");
    Assert.assertTrue(pkg.isFile());
    Assert.assertNotNull(loadPackage(pkg).get("Title"));
  }

  @Test
  @TestFor(issues = "TW-26973")
  public void test_minClientVersion() throws IOException, PackageLoadException {
    final File pkg = Paths.getTestDataPath("packages/Min.Version.Two.Five.1.0.0.nupkg");
    Assert.assertTrue(pkg.isFile());
    Assert.assertNotNull(loadPackage(pkg).get("MinClientVersion"));
  }

  @NotNull
  private Map<String, String> loadPackage(@NotNull File artifact) throws PackageLoadException, IOException {
    final LocalNuGetPackageItemsFactory packageItemsFactory = new LocalNuGetPackageItemsFactory();
    new NuGetPackageStructureVisitor(Lists.newArrayList(packageItemsFactory)).visit(new FileInputStream(artifact));
    return packageItemsFactory.getItems();
  }
}
