# TeamCity NuGet support [![official JetBrains project](http://jb.gg/badges/official-plastic.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This plugin provides NuGet support features for TeamCity. For more details, please see [NuGet support description](https://confluence.jetbrains.com/display/TCDL/NuGet).

# Download

The plugin is bundled from TeamCity 7.0. If you need the latest build, download and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

| Branch | Status | Download | TeamCity |
|--------|--------|----------|----------|
| master | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportForTrunk&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportForTrunk)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportForTrunk/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 2019.1.x |
| Jaipur-2018.2.x | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20182x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20182x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20182x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 2018.2.x |
| Jaipur-2018.1.x | <a href="https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20181x&guest=1"><img src="https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20181x)/statusIcon.svg" alt=""/></a> | [Download](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_NuGet_NuGetSupportFor20181x/.lastSuccessful/dotNetPackagesSupport.zip?guest=1)| 2018.1.x |

# Building the plugin

This project uses gradle as a build system. To resolve non-public libraries, you need to execute 
`:nuget-server:installTeamCity{version}` gradle task or have a local TeamCity installation. 
To build the plugin, you need to specify the TeamCity version in `gradle/libs.versions.toml`.


## Gradle tasks
* `:nuget-extensions:msbuild` - build .net nuget extensions.
* `:nuget-extensions:nunit` - run .net nuget extensions tests.
* `:nuget-server:installTeamCity{version}` - downloads TeamCity distribution with the specified version.
* `:nuget-server:assemble` - assemble nuget support plugin.
* `:nuget-server:build` - build & test nuget support plugin.

## Requirements

On Windows to build nuget extensions should be installed Microsoft Build Tools.
On Linux for that should be installed 'mono-devel' package.

# Contributions
We appreciate all kinds of feedback, so please feel free to send a PR or [file an issue](https://youtrack.jetbrains.com/newIssue?project=TW&c=Subsystem%20plugin%3A%20NuGet).

