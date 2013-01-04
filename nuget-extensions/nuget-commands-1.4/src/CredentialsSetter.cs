using System;
using System.Collections.Generic;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public partial class CredentialsSetter
  {
    private void UpdateCredentials(IEnumerable<NuGetSource> actual)
    {
      var message = string.Format("You use NuGet {0}. Feed authentication is only supported from NuGet 2.0", typeof(ICommand).Assembly.GetName().Version);
      Console.Out.WriteLine("##teamcity[message text='{0}' status='WARNING']", message);      
    }
  }
}
