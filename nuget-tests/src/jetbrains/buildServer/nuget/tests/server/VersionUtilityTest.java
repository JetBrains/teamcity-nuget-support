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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.version.FrameworkName;
import jetbrains.buildServer.nuget.server.version.Version;
import jetbrains.buildServer.nuget.server.version.VersionUtility;
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
}
