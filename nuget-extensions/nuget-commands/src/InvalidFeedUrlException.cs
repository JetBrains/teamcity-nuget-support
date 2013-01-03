using System;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class InvalidFeedUrlException : Exception
  {
    public InvalidFeedUrlException(string feedUrl, string message) : base(string.Format("Speficied feed URI \"{0}\" is invalid. {1}", feedUrl ?? "<null>", message))
    {
    }
  }
}
