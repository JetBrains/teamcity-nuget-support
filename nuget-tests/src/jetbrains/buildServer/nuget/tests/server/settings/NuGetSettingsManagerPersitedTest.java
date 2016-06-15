/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.settings;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsEventAdapter;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerConfiguration;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerImpl;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsPersistance;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsWatcher;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.WaitForAssert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent.SERVER;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 15:56
 */
public class NuGetSettingsManagerPersitedTest extends NuGetSettingsManagerTest {
  private Mockery m;
  private NuGetSettingsManagerConfiguration myConfig;
  private File myFile;
  private NuGetSettingsPersistance myPersistance;
  private NuGetSettingsWatcher myWatcher;
  private EventDispatcher<BuildServerListener> myListener;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myConfig = m.mock(NuGetSettingsManagerConfiguration.class);
    myFile = createTempFile();

    m.checking(new Expectations(){{
      allowing(myConfig).getNuGetConfigXml();
      will(returnValue(myFile));
    }});

    recreateSettings();
  }

  @Override
  protected void recreateSettings() {
    super.recreateSettings();

    if (myConfig == null) return;

    if (myListener != null) {
      myListener.getMulticaster().serverShutdown();
    }

    myPersistance = new NuGetSettingsPersistance(myConfig);
    myListener = EventDispatcher.create(BuildServerListener.class);
    myWatcher = new NuGetSettingsWatcher(myConfig, myListener, myPersistance, (NuGetSettingsManagerImpl) mySettings);
    myWatcher.setWatchInterval(10);

    myListener.getMulticaster().serverStartup();
  }

  @Test
  public void testReadWriteRecreate() {
    testReadWrite();
    recreateSettings();

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(true, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(44, action.getIntParameter("int1", 42));
        Assert.assertEquals("zzz", action.getStringParameter("string1"));
        return null;
      }
    });
  }

  @Test
  public void testRemoveKeyRecreate() {
    testRemoveKey();
    recreateSettings();

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(false, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(42, action.getIntParameter("int1", 42));
        Assert.assertEquals(null, action.getStringParameter("string1"));
        return null;
      }
    });
  }

  @Test
  public void testUpdateKeyRecreate() {
    testUpdateKey();
    recreateSettings();

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(false, action.getBooleanParameter("bool1", true));
        Assert.assertEquals(445, action.getIntParameter("int1", 42));
        Assert.assertEquals("uuu", action.getStringParameter("string1"));
        return null;
      }
    });
  }

  @Test
  public void testReadWrongTypeRecreate() {
    testReadWrongType();
    recreateSettings();

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(true, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(44, action.getIntParameter("bool1", 44));
        Assert.assertEquals("true", action.getStringParameter("bool1"));

        Assert.assertEquals(false, action.getBooleanParameter("int1", false));
        Assert.assertEquals(44, action.getIntParameter("int1", 42));
        Assert.assertEquals("44", action.getStringParameter("int1"));

        Assert.assertEquals("zzz", action.getStringParameter("string1"));
        Assert.assertEquals(444, action.getIntParameter("string1", 444));
        Assert.assertEquals(false, action.getBooleanParameter("string1", false));
        return null;
      }
    });
  }

  @Test
  public void testFileWatcherReload() throws IOException {
    testReadWrite();
    File tmp = createTempFile();
    FileUtil.copy(myFile, tmp);

    FileUtil.delete(myFile);

    recreateSettings();
    myWatcher.setWatchInterval(10);

    mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertNull(action.getStringParameter("string1"));
        return null;
      }
    });

    FileUtil.copy(tmp, myFile);

    new WaitForAssert(){
      @Override
      protected boolean condition() {
        return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Boolean>() {
          public Boolean executeAction(@NotNull NuGetSettingsReader action) {
            return "zzz".equals(action.getStringParameter("string1"));
          }
        });
      }
    };
  }

  @Test
  public void testFileWatcherReload_event() throws IOException, InterruptedException {
    enableDebug();

    testReadWrite();
    File tmp = createTempFile();
    FileUtil.copy(myFile, tmp);

    FileUtil.delete(myFile);

    recreateSettings();
    final AtomicBoolean componentReloadCalled = new AtomicBoolean();
    final AtomicBoolean reloadCalled = new AtomicBoolean();
    mySettings.addListener(new NuGetSettingsEventAdapter() {
      @Override
      public void settingsChanged(@NotNull NuGetSettingsComponent component) {
        componentReloadCalled.set(true);
      }

      @Override
      public void settingsReloaded() {
        reloadCalled.set(true);
      }
    });

    mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertNull(action.getStringParameter("string1"));
        return null;
      }
    });

    Thread.sleep(300);
    FileUtil.copy(tmp, myFile);

    new WaitForAssert(){
      @Override
      protected boolean condition() {
        return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Boolean>() {
          public Boolean executeAction(@NotNull NuGetSettingsReader action) {
            String stringParameter = action.getStringParameter("string1");
            log("Reading string1 parameter. Value - " + stringParameter);
            return "zzz".equals(stringParameter);
          }
        });
      }
    };

    Assert.assertFalse(componentReloadCalled.get());
    Assert.assertTrue(reloadCalled.get());
  }

}
