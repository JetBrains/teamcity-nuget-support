#TeamCity NuGet support

This plugin provides NuGet support features for TeamCity. For more details, please see [NuGet support description](http://confluence.jetbrains.net/display/TW/NuGet+support).

# Download

The plugin is bundled from TeamCity 7.0. If you need a latest build download and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

| Plugin | Status | Download | TeamCity |
|--------|--------|----------|----------|
| 0.15 (dev) | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 10.x |
| 0.14 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 10.0.x |
| 0.13 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 9.1.x |
| 0.12 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 9.0.x |
| 0.11 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 8.1.x |
| 0.10 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGetSupportV010for80&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGetSupportV010for80)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGetSupportV010for80/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 8.0.x |

# License
Apache 2.0

# Building the plugin
- First build .Net part of the plugin:    
     `msbuild nuget-extensions/nuget-extensions.sln /t:Rebuild /p:Configuration=Release`

- Then build Java part and TeamCity plugin distribution:     
     open the sources in InteliJ IDEA and build artifact `plugin-zip`

# Contributions
We appreciate all kinds of feedback, so please feel free to send a PR or write an issue.



