

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.version.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class VersionUtilityTest extends BaseTestCase {
  @Test
  public void isCompatibleReturnsFalseForSlAndWindowsPhoneFrameworks() throws Exception {
    final FrameworkName sl3 = VersionUtility.parseFrameworkName("sl3");
    final FrameworkName wp7 = VersionUtility.parseFrameworkName("sl3-wp");

    assertFalse(VersionUtility.isCompatible(sl3, wp7));
    assertFalse(VersionUtility.isCompatible(wp7, sl3));
  }

  @Test
  public void isCompatibleWindowsPhoneVersions(){
    final FrameworkName wp7 = VersionUtility.parseFrameworkName("sl3-wp");
    final FrameworkName wp7Mango = VersionUtility.parseFrameworkName("sl4-wp71");
    final FrameworkName wp8 = new FrameworkName("WindowsPhone", Version.valueOf("8.0"), "");
    final FrameworkName wp81 = new FrameworkName("WindowsPhone", Version.valueOf("8.1"), "");
    final FrameworkName wpa81 = VersionUtility.parseFrameworkName("wpa81");

    assertFalse(VersionUtility.isCompatible(wp7, wp7Mango));
    assertTrue(VersionUtility.isCompatible(wp7Mango, wp7));

    assertTrue(VersionUtility.isCompatible(wp8, wp7));
    assertTrue(VersionUtility.isCompatible(wp8, wp7Mango));

    assertFalse(VersionUtility.isCompatible(wp7, wp8));
    assertFalse(VersionUtility.isCompatible(wp7Mango, wp8));

    assertTrue(VersionUtility.isCompatible(wp81, wp8));

    assertFalse(VersionUtility.isCompatible(wpa81, wp81));
  }

  @DataProvider
  public Object[][] wf7() {
    return new Object[][] { {"wp"}, {"wp7"}, {"wp70"}, {"windowsphone"}, {"windowsphone7"}, {"windowsphone70"}, {"sl3-wp"} };
  }

  @Test(dataProvider = "wf7")
  public void windowsPhone7IdentifierCompatibleWithAllWPProjects(String wp7Identifier){
    final FrameworkName wp7Package = VersionUtility.parseFrameworkName(wp7Identifier);
    final FrameworkName wp7Project = new FrameworkName("Silverlight", Version.valueOf("3.0"), "WindowsPhone");
    final FrameworkName mangoProject = new FrameworkName("Silverlight", Version.valueOf("4.0"), "WindowsPhone71");
    final FrameworkName apolloProject = new FrameworkName("WindowsPhone", Version.valueOf("8.0"), "");

    assertTrue(VersionUtility.isCompatible(wp7Project, wp7Package));
    assertTrue(VersionUtility.isCompatible(mangoProject, wp7Package));
    assertTrue(VersionUtility.isCompatible(apolloProject, wp7Package));
  }

  @DataProvider
  public Object[][] wf71() {
    return new Object[][] { {"wp71"}, {"windowsphone71"}, {"sl4-wp71"} };
  }

  @Test(dataProvider = "wf71")
  public void windowsPhoneMangoIdentifierCompatibleWithAllWPProjects(String mangoIdentifier){
    final FrameworkName mangoPackage = VersionUtility.parseFrameworkName(mangoIdentifier);
    final FrameworkName wp7Project = new FrameworkName("Silverlight", Version.valueOf("3.0"), "WindowsPhone");
    final FrameworkName mangoProject = new FrameworkName("Silverlight", Version.valueOf("4.0"), "WindowsPhone71");
    final FrameworkName apolloProject = new FrameworkName("WindowsPhone", Version.valueOf("8.0"), "");

    assertFalse(VersionUtility.isCompatible(wp7Project, mangoPackage));
    assertTrue(VersionUtility.isCompatible(mangoProject, mangoPackage));
    assertTrue(VersionUtility.isCompatible(apolloProject, mangoPackage));
  }

  @DataProvider
  public Object[][] wf8() {
    return new Object[][] { {"wp8"}, {"wp80"}, {"windowsphone8"}, {"windowsphone80"} };
  }

  @Test(dataProvider = "wf8")
  public void windowsPhoneApolloIdentifierCompatibleWithAllWPProjects(String apolloIdentifier){
    final FrameworkName apolloPackage = VersionUtility.parseFrameworkName(apolloIdentifier);

    final FrameworkName wp7Project = new FrameworkName("Silverlight", Version.valueOf("3.0"), "WindowsPhone");
    final FrameworkName mangoProject = new FrameworkName("Silverlight", Version.valueOf("4.0"), "WindowsPhone71");
    final FrameworkName apolloProject = new FrameworkName("WindowsPhone", Version.valueOf("8.0"), "");

    assertFalse(VersionUtility.isCompatible(wp7Project, apolloPackage));
    assertFalse(VersionUtility.isCompatible(mangoProject, apolloPackage));
    assertTrue(VersionUtility.isCompatible(apolloProject, apolloPackage));
  }

  @DataProvider
  public Object[][] windows() {
    return new Object[][] { {"windows"}, {"windows8"}, {"win"}, {"win8"} };
  }

  @Test(dataProvider = "windows")
  public void windowsIdentifierCompatibleWithWindowsStoreAppProjects(String identifier){
    final FrameworkName packageFramework = VersionUtility.parseFrameworkName(identifier);
    final FrameworkName projectFramework = new FrameworkName(".NETCore", Version.valueOf("4.5"), "");
    assertTrue(VersionUtility.isCompatible(projectFramework, packageFramework));
  }

  @DataProvider
  public Object[][] windows_unsupported() {
    return new Object[][] { {"windows9"}, {"win9"}, {"win10"}, {"windows81"}, {"windows45"}, {"windows1"} };
  }

  @Test(dataProvider = "windows_unsupported")
  public void windowsIdentifierWithUnsupportedVersionNotCompatibleWithWindowsStoreAppProjects(String identifier){
    final FrameworkName packageFramework = VersionUtility.parseFrameworkName(identifier);
    final FrameworkName projectFramework = new FrameworkName(".NETCore", Version.valueOf("4.5"), "");
    assertFalse(VersionUtility.isCompatible(projectFramework, packageFramework));
  }

  @Test
  public void netFrameworkCompatibiilityIsCompatibleReturns(){
    final FrameworkName net40 = VersionUtility.parseFrameworkName("net40");
    final FrameworkName net40Client = VersionUtility.parseFrameworkName("net40-client");
    assertTrue(VersionUtility.isCompatible(net40, net40Client));
    assertTrue(VersionUtility.isCompatible(net40Client, net40));
  }

  @Test
  public void lowerFrameworkVersionsAreNotCompatibleWithHigherFrameworkVersionsWithSameFrameworkName(){
    final FrameworkName net40 = VersionUtility.parseFrameworkName("net40");
    final FrameworkName net20 = VersionUtility.parseFrameworkName("net20");
    assertFalse(VersionUtility.isCompatible(net20, net40));
    assertTrue(VersionUtility.isCompatible(net40, net20));
  }

  @Test
  public void isCompatibleReturnsTrueIfSupportedFrameworkNull(){
    assertTrue(VersionUtility.isCompatible(VersionUtility.parseFrameworkName("net40-client"), null));
  }

  @Test
  public void isCompatibleReturnsTrueIfProjectFrameworkIsNull(){
    assertTrue(VersionUtility.isCompatible(null, VersionUtility.parseFrameworkName("net40-client")));
  }

  @Test
  public void testParsePortable() throws Exception {
    final FrameworkName frameworkName = VersionUtility.parseFrameworkName("portable-windows8+net45");
    assertNotNull(frameworkName);
    assertEquals(VersionUtility.PORTABLE_FRAMEWORK_IDENTIFIER, frameworkName.getIdentifier());
    assertEquals(Version.EMPTY, frameworkName.getVersion());
    assertEquals("windows8+net45", frameworkName.getProfile());
  }

  @Test
  public void testNormalizeVersion() throws Exception {
    assertEquals("1.0.0", VersionUtility.normalizeVersion("1"));
    assertEquals("2.0.0", VersionUtility.normalizeVersion("2.0"));
    assertEquals("3.0.0", VersionUtility.normalizeVersion("3.0.0.0"));
    assertEquals("4.0.0-alpha", VersionUtility.normalizeVersion("4.0.0.0-alpha"));
    assertEquals("5.0.0", VersionUtility.normalizeVersion("05.0"));
    assertEquals("6.2.1.1", VersionUtility.normalizeVersion("6.2.01.1"));
  }

  @Test
  public void testValueOfNonSemVer() {
    PackageVersion version1 = VersionUtility.valueOf("v1");
    Assert.assertEquals(version1.getLevel(), SemVerLevel.NONE);
  }

  @Test
  public void testValueOfSemVer1() {
    PackageVersion version1 = VersionUtility.valueOf("1.0.0");
    Assert.assertEquals(version1.getLevel(), SemVerLevel.V1);

    PackageVersion version2 = VersionUtility.valueOf("1.0.0-beta");
    Assert.assertEquals(version2.getLevel(), SemVerLevel.V1);
  }

  @Test
  public void testValueOfSemVer2() {
    PackageVersion version1 = VersionUtility.valueOf("1.0.0+metadata");
    Assert.assertEquals(version1.getLevel(), SemVerLevel.V2);

    PackageVersion version2 = VersionUtility.valueOf("1.0.0-rel.2");
    Assert.assertEquals(version2.getLevel(), SemVerLevel.V2);
  }

  @Test
  void normalizeSemVer2() {
    Assert.assertEquals(VersionUtility.normalizeVersion("1.0.01"), "1.0.1");
    Assert.assertEquals(VersionUtility.normalizeVersion("1.0.0.0"), "1.0.0");
    Assert.assertEquals(VersionUtility.normalizeVersion("1.0"), "1.0.0");
    Assert.assertEquals(VersionUtility.normalizeVersion("1.0.0+BuildAgent1"), "1.0.0");
    Assert.assertEquals(VersionUtility.normalizeVersion("1.0.0-alpha.1.2.30+BuildAgent1"), "1.0.0-alpha.1.2.30");
  }

  @DataProvider
  public Object[][] frameworkNames() {
    return new Object[][]{
      {".NETPlatform5.0", new FrameworkName(".NETPlatform", new Version(5,0,0), "")},
      {"Windows8.0", new FrameworkName("Windows", new Version(8,0,0), "")},
      {".NETFramework4.5", new FrameworkName(".NETFramework", new Version(4,5,0), "")},
      {"MonoAndroid1.0", new FrameworkName("MonoAndroid", new Version(1,0,0), "")},
      {"MonoTouch1.0", new FrameworkName("MonoTouch", new Version(1,0,0), "")},
      {"WindowsPhone8.0", new FrameworkName("WindowsPhone", new Version(8,0,0), "")},
      {"WindowsPhoneApp8.1", new FrameworkName("WindowsPhoneApp", new Version(8,1,0), "")},
      {"Xamarin.iOS1.0", new FrameworkName("Xamarin.iOS", new Version(1,0,0), "")},
      {"Xamarin.Mac2.0", new FrameworkName("Xamarin.Mac", new Version(2,0,0), "")},
      {".NETCore5.0", new FrameworkName(".NETCore", new Version(5,0,0), "")},
      {".NETStandard1.3", new FrameworkName(".NETStandard", new Version(1,3,0), "")},
      {".NETCoreApp2.0", new FrameworkName(".NETCoreApp", new Version(2,0,0), "")},
      {"net462", new FrameworkName(".NETFramework", new Version(4,6,2), "")},
      {"sl3-wp", new FrameworkName("Silverlight", new Version(3,0,0), "WindowsPhone")},
      {"sl4-wp71", new FrameworkName("Silverlight", new Version(4,0,0), "WindowsPhone71")},
      {"wpa81", new FrameworkName("WindowsPhoneApp", new Version(8,1,0), "")},
      {"windowsphone70", new FrameworkName("WindowsPhone", new Version(7,0,0), "")},
      {"netcore451", new FrameworkName(".NETCore", new Version(4,5,1), "")},
      {"netmf", new FrameworkName(".NETMicroFramework", new Version(0,0,0), "")},
      {"win10", new FrameworkName("Windows", new Version(1,0,0), "")},
      {"wp75", new FrameworkName("WindowsPhone", new Version(7,5,0), "")},
      {"netstandard1.6", new FrameworkName(".NETStandard", new Version(1,6,0), "")},
      {"netcoreapp1.1", new FrameworkName(".NETCoreApp", new Version(1,1,0), "")},
      {"uap10.0", new FrameworkName("uap", new Version(10,0,0), "")},
      {"blah", null}
    };
  }

  @Test(dataProvider = "frameworkNames")
  void testParseFrameworkName(@NotNull final String name, @Nullable FrameworkName framework) {
    final FrameworkName result = VersionUtility.parseFrameworkName(name);
    if (framework == null) {
      Assert.assertNull(result);
    } else {
      Assert.assertNotNull(result);
      Assert.assertEquals(result, framework);
    }
  }
}
