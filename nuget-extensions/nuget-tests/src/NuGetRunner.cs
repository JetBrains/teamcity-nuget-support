using System;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class NuGetRunner
  {
    public static readonly Lazy<string> Path = new Lazy<string>(()=>typeof (Program).Assembly.GetAssemblyPath());
  }
}