using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class InvalidFeedUrlException : Exception
  {
    public InvalidFeedUrlException(string feedUrl, string message) : base(string.Format("Speficied feed URI \"{0}\" is invalid. {1}", feedUrl ?? "<null>", message))
    {
    }
  }
}
