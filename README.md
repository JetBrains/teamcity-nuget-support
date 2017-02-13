#TeamCity NuGet support

This plugin provides NuGet support features for TeamCity. For more details, please see [NuGet support description](https://confluence.jetbrains.com/display/TCDL/NuGet).

# Download

The plugin is bundled from TeamCity 7.0. If you need the latest build, download and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

| Plugin | Status | Download | TeamCity |
|--------|--------|----------|----------|
| 0.15 (dev) | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV015for10x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 10.x |
| 0.14 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV014for100x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 10.0.x |
| 0.13 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV013for91x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 9.1.x |
| 0.12 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV012for90/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 9.0.x |
| 0.11 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportV011for8/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 8.1.x |
| 0.10 | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGetSupportV010for80&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGetSupportV010for80)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGetSupportV010for80/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 8.0.x |

# Building the plugin
This project uses gradle as a build system. To resolve non-public libraries, you need to have a local TeamCity installation and define `teamcityDir` in the [gradle properties](https://docs.gradle.org/current/userguide/build_environment.html). After that you can open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

## Gradle tasks
* `:nuget-extensions:msbuild` - build .net nuget extensions.
* `:nuget-extensions:nunit` - run .net nuget extensions tests.
* `:nuget-server:assemble` - assemble nuget support plugin.
* `:nuget-server:build` - build & test nuget support plugin.

# Contributions
We appreciate all kinds of feedback, so please feel free to send a PR or write [an issue](https://github.com/JetBrains/teamcity-nuget-support/issues).

# License
Apache 2.0
