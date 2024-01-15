

package jetbrains.buildServer.nuget.tests.server.settings;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.settings.*;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent.SERVER;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:22
 */
public class NuGetSettingsManagerTest extends BaseTestCase {
  protected NuGetSettingsManager mySettings;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    recreateSettings();
  }

  protected void recreateSettings() {
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
        action.setIntParameter("int1", 44);
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
  }

  @Test
  public void testComponentChanged() {
    final AtomicBoolean called = new AtomicBoolean();
    mySettings.addListener(new NuGetSettingsEventAdapter(){
      @Override
      public void settingsChanged(@NotNull NuGetSettingsComponent component) {
        called.set(true);
      }
    });

    testReadWrite();

    Assert.assertTrue(called.get());
  }

  @Test
  public void testComponentChangedNoReload() {
    final AtomicBoolean called = new AtomicBoolean();
    mySettings.addListener(new NuGetSettingsEventAdapter(){
      @Override
      public void settingsReloaded() {
        called.set(true);
      }
    });

    testReadWrite();

    Assert.assertFalse(called.get());
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
        Assert.assertEquals(44, action.getIntParameter("int1", 42));
        Assert.assertEquals("44", action.getStringParameter("int1"));

        Assert.assertEquals("zzz", action.getStringParameter("string1"));
        Assert.assertEquals(444, action.getIntParameter("string1", 444));
        Assert.assertEquals(false, action.getBooleanParameter("string1", false));
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
