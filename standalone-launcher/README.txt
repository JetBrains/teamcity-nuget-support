NuGet Java Feed
================

This is a simple Java application to host your 
own NuGet Feed. All you need to have is 
a folder with NuGet packages (.nupkg files) 
and Java runtime.

To start the server use the following commandline:

    java -jar standalonge-nuget-feed.jar <path to packages folder> [/url:<server url>]

Where:
 <path to packages folder>  is path to .nupkg files
 <server url>               is url to start server for, i.e. http://localhost:8888/nuget/feed


The server will monitor any changes to in the packages 
folder to synchronize with the feed.

Feel free to contribute/comment/bug-report for the project at
https://github.com/JetBrains/teamcity-nuget-support/tree/standalone3

License
========
Apache 2.0


Source code
===========
Project is hosted on GitHub at
https://github.com/JetBrains/teamcity-nuget-support/tree/standalone3


Third-party license
==================
Dustribution includes several open-source libraries. 
Please refer to licenses folder for details.


