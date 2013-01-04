using NuGet.Commands;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class NuGetTeamCityInfo : ICreatableComponent
  {
    public void Initialize()
    {
      
    }

    public void LogRuntimeInfo()
    {
      System.Console.Out.WriteLine("TeamCity NuGet Extension is available.");
      System.Console.Out.WriteLine("NuGet Version = {0}", typeof(Command).Assembly.GetName().Version);
      System.Console.Out.WriteLine("TeamCity Extension = {0}", GetType().Assembly.GetName().FullName);
    }

    public string Describe()
    {
      return "";
    }
  }
}