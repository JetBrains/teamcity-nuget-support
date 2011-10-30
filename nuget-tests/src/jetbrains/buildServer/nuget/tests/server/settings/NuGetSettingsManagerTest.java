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

package jetbrains.buildServer.nuget.tests.server.settings;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent.SERVER;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:22
 */
public class NuGetSettingsManagerTest extends BaseTestCase {
  private NuGetSettingsManager mySettings;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    recreateSettings();
  }

  private void recreateSettings() {
    mySettings = new NuGetSettingsManagerImpl();
  }

  @Test
  public void testReadEmpty() {
    mySettings.readSettings(SERVER, checkDefaults());
  }

  @Test
  public void testReadWrite() {
    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setBooleanParameter("bool1", true);
        action.setIntParameter("int1", 239);
        action.setStringParameter("string1", "zzz");
        return null;
      }
    });

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(true, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(44, action.getIntParameter("int1", 42));
        Assert.assertEquals("zzz", action.getStringParameter("string1"));
        return null;
      }
    });

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
  public void testRemoveKey() {
    testReadWrite();

    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.removeParameter("bool1");
        action.removeParameter("int1");
        action.removeParameter("string1");
        return null;
      }
    });

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(false, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(42, action.getIntParameter("int1", 42));
        Assert.assertEquals(null, action.getStringParameter("string1"));
        return null;
      }
    });

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
  public void testUpdateKey() {
    testReadWrite();

    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setBooleanParameter("bool1", false);
        action.setIntParameter("int1", 445);
        action.setStringParameter("string1", "uuu");
        return null;
      }
    });

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(false, action.getBooleanParameter("bool1", true));
        Assert.assertEquals(445, action.getIntParameter("int1", 42));
        Assert.assertEquals("uuu", action.getStringParameter("string1"));
        return null;
      }
    });

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
  public void testReadWrongType() {
    testReadWrite();

    mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(true, action.getBooleanParameter("bool1", false));
        Assert.assertEquals(44, action.getIntParameter("bool1", 44));
        Assert.assertEquals("true", action.getStringParameter("bool1"));

        Assert.assertEquals(false, action.getBooleanParameter("int1", false));
        Assert.assertEquals(445, action.getIntParameter("int1", 42));
        Assert.assertEquals("445", action.getStringParameter("int1"));

        Assert.assertEquals("uuu", action.getStringParameter("string1"));
        Assert.assertEquals(444, action.getIntParameter("string1",444));
        Assert.assertEquals(false, action.getBooleanParameter("string1",false));
        return null;
      }
    });
  }

  @NotNull
  private NuGetSettingsManager.Func<NuGetSettingsReader, Object> checkDefaults() {
    return new NuGetSettingsManager.Func<NuGetSettingsReader, Object>() {
      public Object executeAction(@NotNull NuGetSettingsReader action) {
        Assert.assertEquals(true, action.getBooleanParameter("bool", true));
        Assert.assertEquals(42, action.getIntParameter("int", 42));
        Assert.assertEquals(null, action.getStringParameter("foo"));
        return null;
      }
    };
  }


}
