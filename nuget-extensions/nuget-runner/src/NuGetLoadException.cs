using System;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetLoadException : Exception
  {
    public NuGetLoadException(string message) : base(message)
    {
    }

    public NuGetLoadException(string message, Exception innerException) : base(message, innerException)
    {
    }
  }
}