using System;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RemoteException : Exception
  {
    public RemoteException(string message) : base(message)
    {
    }

    public RemoteException(string message, Exception innerException) : base(message, innerException)
    {
    }
  }
}