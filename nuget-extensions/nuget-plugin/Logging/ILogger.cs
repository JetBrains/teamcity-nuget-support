// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using NuGet.Common;

namespace JetBrains.TeamCity.NuGet.Logging
{
  public interface ILogger
  {
    void Log(LogLevel level, string message);

    void SetLogLevel(LogLevel newLogLevel);
  }
}
