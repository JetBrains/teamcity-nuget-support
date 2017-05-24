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

package jetbrains.buildServer.nuget.server.version;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.CaseInsensitiveMap;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snapshot of http://nuget.codeplex.com/SourceControl/latest#src/Core/Utility/VersionUtility.cs
 * Nuget 2.8.3 version
 * @author Evgeniy.Koshkin
 */
public class VersionUtility {

  private static final Logger LOG = Logger.getInstance(VersionUtility.class.getName());

  private static final String NET_FRAMEWORK_IDENTIFIER = ".NETFramework";
  private static final String NET_CORE_FRAMEWORK_IDENTIFIER = ".NETCore";
  public static final String PORTABLE_FRAMEWORK_IDENTIFIER = ".NETPortable";
  private static final String ASP_NET_FRAMEWORK_IDENTIFIER = "ASP.Net";
  private static final String ASP_NET_CORE_FRAMEWORK_IDENTIFIER = "ASP.NetCore";
  private static final String SILVERLIGHT_IDENTIFIER = "Silverlight";

  private static final Map<String, String> KNOWN_IDENTIFIERS = new CaseInsensitiveMap<String>(CollectionsUtil.asMap(
          // .NET Desktop
          "NET", NET_FRAMEWORK_IDENTIFIER,
          ".NET", NET_FRAMEWORK_IDENTIFIER,
          "NETFramework", NET_FRAMEWORK_IDENTIFIER,
          ".NETFramework", NET_FRAMEWORK_IDENTIFIER,

          // .NET Core
          "NETCore", NET_CORE_FRAMEWORK_IDENTIFIER,
          ".NETCore", NET_CORE_FRAMEWORK_IDENTIFIER,
          "WinRT", NET_CORE_FRAMEWORK_IDENTIFIER,     // 'WinRT' is now deprecated. Use 'Windows' or 'win' instead.

          // .NET Micro Framework
          ".NETMicroFramework", ".NETMicroFramework",
          "netmf", ".NETMicroFramework",

          // Silverlight
          "SL", SILVERLIGHT_IDENTIFIER,
          SILVERLIGHT_IDENTIFIER, SILVERLIGHT_IDENTIFIER,

          // Portable Class Libraries
          ".NETPortable", PORTABLE_FRAMEWORK_IDENTIFIER,
          "NETPortable", PORTABLE_FRAMEWORK_IDENTIFIER,
          "portable", PORTABLE_FRAMEWORK_IDENTIFIER,

          // Windows Phone
          "wp", "WindowsPhone",
          "WindowsPhone", "WindowsPhone",
          "WindowsPhoneApp", "WindowsPhoneApp",
          "wpa", "WindowsPhoneApp",

          // Windows
          "Windows", "Windows",
          "win", "Windows",

          // ASP.Net
          "aspnet", ASP_NET_FRAMEWORK_IDENTIFIER,
          "aspnetcore", ASP_NET_CORE_FRAMEWORK_IDENTIFIER,
          "asp.net", ASP_NET_FRAMEWORK_IDENTIFIER,
          "asp.netcore", ASP_NET_CORE_FRAMEWORK_IDENTIFIER,

          // Native
          "native", "native",

          // Mono/Xamarin
          "MonoAndroid", "MonoAndroid",
          "MonoTouch", "MonoTouch",
          "MonoMac", "MonoMac",
          "Xamarin.iOS", "Xamarin.iOS",
          "XamariniOS", "Xamarin.iOS",
          "Xamarin.Mac", "Xamarin.Mac",
          "XamarinMac", "Xamarin.Mac",
          "Xamarin.PlayStationThree", "Xamarin.PlayStation3",
          "XamarinPlayStationThree", "Xamarin.PlayStation3",
          "XamarinPSThree", "Xamarin.PlayStation3",
          "Xamarin.PlayStationFour", "Xamarin.PlayStation4",
          "XamarinPlayStationFour", "Xamarin.PlayStation4",
          "XamarinPSFour", "Xamarin.PlayStation4",
          "Xamarin.PlayStationVita", "Xamarin.PlayStationVita",
          "XamarinPlayStationVita", "Xamarin.PlayStationVita",
          "XamarinPSVita", "Xamarin.PlayStationVita",
          "Xamarin.XboxThreeSixty", "Xamarin.Xbox360",
          "XamarinXboxThreeSixty", "Xamarin.Xbox360",
          "Xamarin.XboxOne", "Xamarin.XboxOne",
          "XamarinXboxOne", "Xamarin.XboxOne"));

  private static final Map<String, String> KNOWN_PROFILES =  new CaseInsensitiveMap<String>(CollectionsUtil.asMap(
          "Client", "Client",
          "WP", "WindowsPhone",
          "WP71", "WindowsPhone71",
          "CF", "CompactFramework",
          "Full",""
  ));

  private static final Map<FrameworkName, FrameworkName> FRAMEWORK_NAME_ALIAS = CollectionsUtil.asMap(
          new FrameworkName("WindowsPhone", Version.valueOf("0.0"), ""), new FrameworkName(SILVERLIGHT_IDENTIFIER, Version.valueOf("3.0"), "WindowsPhone"),
          new FrameworkName("WindowsPhone", Version.valueOf("7.0"), ""), new FrameworkName(SILVERLIGHT_IDENTIFIER, Version.valueOf("3.0"), "WindowsPhone"),
          new FrameworkName("WindowsPhone", Version.valueOf("7.1"), ""), new FrameworkName(SILVERLIGHT_IDENTIFIER, Version.valueOf("4.0"), "WindowsPhone71"),
          new FrameworkName("WindowsPhone", Version.valueOf("8.0"), ""), new FrameworkName(SILVERLIGHT_IDENTIFIER, Version.valueOf("8.0"), "WindowsPhone"),
          new FrameworkName("WindowsPhone", Version.valueOf("8.1"), ""), new FrameworkName(SILVERLIGHT_IDENTIFIER, Version.valueOf("8.1"), "WindowsPhone"),
          new FrameworkName("Windows", Version.valueOf("0.0"), ""), new FrameworkName(".NETCore", Version.valueOf("4.5"), ""),
          new FrameworkName("Windows", Version.valueOf("8.0"), ""), new FrameworkName(".NETCore", Version.valueOf("4.5"), ""),
          new FrameworkName("Windows", Version.valueOf("8.1"), ""), new FrameworkName(".NETCore", Version.valueOf("4.5.1"), "")
  );

  private static final Map<String, FrameworkName> EQUIVALENT_PROJECT_FRAMEWORKS = new CaseInsensitiveMap<FrameworkName>(Collections.singletonMap(
          ASP_NET_FRAMEWORK_IDENTIFIER, new FrameworkName(NET_FRAMEWORK_IDENTIFIER, Version.EMPTY, "")
  ));

  private static final Map<String, Map<String, String>> COMPATIBILTY_MAPPINGS = getCompatibilityMappings();

  private static Map<String, Map<String, String>> getCompatibilityMappings() {
    Map<String, Map<String, String>> mappings = new CaseInsensitiveMap<Map<String, String>>();
    mappings.put(NET_FRAMEWORK_IDENTIFIER, CollectionsUtil.asMap(
            "Client", "",
            "", "Client"));
    mappings.put(SILVERLIGHT_IDENTIFIER, CollectionsUtil.asMap(
            "WindowsPhone", "WindowsPhone71",
            "WindowsPhone71", "WindowsPhone"
    ));
    return mappings;
  }

  private static final String PROFILE_PART_SEPARATOR = "-";
  private static final Pattern VERSION_MATCHING_PATTERN = Pattern.compile("\\d+");

  public static boolean isKnownFramework(@NotNull String frameworkString) {
    return parseFrameworkName(frameworkString) != null;
  }

  public static boolean isCompatible(FrameworkName projectFrameworkName, FrameworkName packageTargetFrameworkName) {
    if (projectFrameworkName == null || packageTargetFrameworkName == null) return true;

    projectFrameworkName = normalizeFrameworkName(projectFrameworkName);
    packageTargetFrameworkName = normalizeFrameworkName(packageTargetFrameworkName);

    final String projectFrameworkIdentifier = projectFrameworkName.getIdentifier();
    final String packageTargetFrameworkIdentifier = packageTargetFrameworkName.getIdentifier();

    if (!projectFrameworkIdentifier.equalsIgnoreCase(packageTargetFrameworkIdentifier)){
      if (EQUIVALENT_PROJECT_FRAMEWORKS.containsKey(projectFrameworkIdentifier)){
        FrameworkName equivalentFrameworkName = EQUIVALENT_PROJECT_FRAMEWORKS.get(projectFrameworkIdentifier);
        if(equivalentFrameworkName.getIdentifier().equalsIgnoreCase(packageTargetFrameworkIdentifier)){
          projectFrameworkName = equivalentFrameworkName;
        }
      }
      else return false;
    }

    final Version projectFrameworkVersion = projectFrameworkName.getVersion();
    final Version packageTargetFrameworkVersion = packageTargetFrameworkName.getVersion();
    if (projectFrameworkVersion != null && packageTargetFrameworkVersion != null && projectFrameworkVersion.compareTo(packageTargetFrameworkVersion) < 0){
      return false;
    }

    final String projectFrameworkProfile = projectFrameworkName.getProfile();
    final String packageTargetFrameworkProfile = packageTargetFrameworkName.getProfile();

    if (StringUtil.areEqual(projectFrameworkProfile, packageTargetFrameworkProfile)){
      return true;
    }

    if (COMPATIBILTY_MAPPINGS.containsKey(projectFrameworkName.getIdentifier())){
      final Map<String, String> mapping = COMPATIBILTY_MAPPINGS.get(projectFrameworkName.getIdentifier());
      if (mapping.containsKey(packageTargetFrameworkProfile)) {
        return mapping.get(packageTargetFrameworkProfile).equalsIgnoreCase(projectFrameworkProfile);
      }
    }

    return false;
  }

  @Nullable
  public static FrameworkName parseFrameworkName(@NotNull String frameworkNameString) {
    final String[] frameworkStringParts = frameworkNameString.split(PROFILE_PART_SEPARATOR);
    if (frameworkStringParts.length > 2) return null;

    final String frameworkNameAndVersion = frameworkStringParts.length > 0 ? frameworkStringParts[0].trim() : null;
    if(StringUtil.isEmpty(frameworkNameAndVersion)) return null;

    String profilePart = frameworkStringParts.length > 1 ? frameworkStringParts[1].trim() : "";
    String identifierPart;
    String versionPart = null;

    final Matcher matcher = VERSION_MATCHING_PATTERN.matcher(frameworkNameAndVersion);

    if (matcher.find()) {
      identifierPart = frameworkNameAndVersion.substring(0, matcher.start()).trim();
      versionPart = frameworkNameAndVersion.substring(matcher.start()).trim();
    }
    else {
      identifierPart = frameworkNameAndVersion.trim();
    }

    if (!StringUtil.isEmpty(identifierPart)){
      if (!KNOWN_IDENTIFIERS.containsKey(identifierPart)) return null;
      identifierPart = KNOWN_IDENTIFIERS.get(identifierPart);
    }

    if (!StringUtil.isEmpty(profilePart)){
      if (KNOWN_PROFILES.containsKey(profilePart))
        profilePart = KNOWN_PROFILES.get(profilePart);
    }

    Version version = Version.EMPTY;

    if(versionPart != null){
      try{
        Integer.parseInt(versionPart);
        if (versionPart.length() > 4){
          versionPart = versionPart.substring(0, 4);
        }
        // Make sure it has at least 2 digits so it parses as a valid version
        versionPart = StringUtils.join(splitByChar(StringUtils.rightPad(versionPart, 2, '0')), ".");
      } catch (NumberFormatException ex){
        LOG.debug("Failed to parse framework version from string " + frameworkNameString, ex);
        return null;
      }

      version = Version.valueOf(versionPart);
      if(version == null){
        if (StringUtil.isEmpty(identifierPart) || !StringUtil.isEmpty(versionPart)){
          return null;
        }
        version = Version.EMPTY;
      }
    }

    if (StringUtil.isEmpty(identifierPart)){
      identifierPart = NET_FRAMEWORK_IDENTIFIER;
    }

    return new FrameworkName(identifierPart, version, profilePart);
  }

  public static boolean isPackageCompatibleWithFrameworks(Set<String> projectFrameworks, final Set<String> packageFrameworkConstraints) {
    if(packageFrameworkConstraints.isEmpty()) return true; //package is compatible with all the frameworks
    if(projectFrameworks.isEmpty()) return true; //no project-level constraints were provided
    final List<FrameworkName> frameworksParsed = CollectionsUtil.convertAndFilterNulls(projectFrameworks, new Converter<FrameworkName, String>() {
      public FrameworkName createFrom(@NotNull String source) {
        return parseFrameworkName(source);
      }
    });
    for(final FrameworkName packageSupportedFramework : CollectionsUtil.convertAndFilterNulls(packageFrameworkConstraints, new Converter<FrameworkName, String>() {
      public FrameworkName createFrom(@NotNull String source) {
        return parseFrameworkName(source);
      }
    })){
      if(CollectionsUtil.contains(frameworksParsed, new Filter<FrameworkName>() {
        public boolean accept(@NotNull FrameworkName framework) {
          return isCompatible(framework, packageSupportedFramework);
        }
      })) return true;
    }
    return false;
  }

  @Nullable
  public static String normalizeVersion(@NotNull String versionString) {
    final SemanticVersion semanticVersion = SemanticVersion.valueOf(versionString);
    if(semanticVersion == null) return null;
    final Version version = semanticVersion.getVersion();
    return String.format("%d.%d.%d%s%s",
            version.getMajor(),
            version.getMinor(),
            version.getPatch(),
            version.getBuild() > 0 ? ("." + String.valueOf(version.getBuild())) : "",
            !StringUtil.isEmpty(semanticVersion.getSpecialVersion()) ? ("-" + semanticVersion.getSpecialVersion()) : "");
  }

  private static FrameworkName normalizeFrameworkName(FrameworkName framework){
    return FRAMEWORK_NAME_ALIAS.containsKey(framework) ? FRAMEWORK_NAME_ALIAS.get(framework) : framework;
  }

  private static String[] splitByChar(String text) {
    String[] result = new String[text.length()];
    for (int i = 0; i < result.length; i++) {
      result[i] = text.substring(i, i + 1);
    }
    return result;
  }
}
