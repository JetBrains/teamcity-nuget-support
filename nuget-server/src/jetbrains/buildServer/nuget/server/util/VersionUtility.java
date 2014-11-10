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

package jetbrains.buildServer.nuget.server.util;

import jetbrains.buildServer.util.CaseInsensitiveMap;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snapshot of http://nuget.codeplex.com/SourceControl/latest#src/Core/Utility/VersionUtility.cs
 * Nuget 2.8.3 version
 * @author Evgeniy.Koshkin
 */
public class VersionUtility {

  private static final String NET_FRAMEWORK_IDENTIFIER = ".NETFramework";
  private static final String NET_CORE_FRAMEWORK_IDENTIFIER = ".NETCore";
  private static final String PORTABLE_FRAMEWORK_IDENTIFIER = ".NETPortable";
  private static final String ASP_NET_FRAMEWORK_IDENTIFIER = "ASP.Net";
  private static final String ASP_NET_CORE_FRAMEWORK_IDENTIFIER = "ASP.NetCore";

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
          "SL", "Silverlight",
          "Silverlight", "Silverlight",

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

  private static final String PROFILE_PART_SEPARATOR = "-";
  private static final Pattern VERSION_MATCHING_PATTERN = Pattern.compile("\\d+");

  public static boolean isKnownFramework(@NotNull String frameworkString) {
    // Split the framework name into 3 parts, identifier, version and profile.
    final String[] frameworkStringParts = frameworkString.split(PROFILE_PART_SEPARATOR);
    if (frameworkStringParts.length > 2) return false;
    final String frameworkNameAndVersion = frameworkStringParts.length > 0 ? frameworkStringParts[0].trim() : null;
    if(StringUtil.isEmpty(frameworkNameAndVersion)) return false;
    final Matcher matcher = VERSION_MATCHING_PATTERN.matcher(frameworkNameAndVersion);
    String identifierPart = matcher.find() ? frameworkNameAndVersion.substring(0, matcher.start()).trim() : frameworkNameAndVersion.trim();
    return !(!StringUtil.isEmpty(identifierPart) && !KNOWN_IDENTIFIERS.containsKey(identifierPart));
  }
}
