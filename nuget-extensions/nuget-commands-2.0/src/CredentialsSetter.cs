using System.ComponentModel.Composition;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Export]
  public class CredentialsSetter : CommandBase
  {
    public CredentialsSetter()
    {
    }

    protected override void ExecuteCommandImpl()
    {
      System.Console.Out.WriteLine("Credentials command");
    }
  }
}
