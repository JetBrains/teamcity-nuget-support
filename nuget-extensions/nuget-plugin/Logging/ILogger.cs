// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using JetBrains.TeamCity.NuGet.Compatibility.Logging;

namespace JetBrains.TeamCity.NuGet.Logging
{
  public interface ILogger
  {
    void Log(LogLevel level, string message, bool notifyNuGet = true);

    void SetLogLevel(LogLevel newLogLevel);
  }
}
