using System;
using NuGet;
using NuGet.Commands;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract class CommandBase : Command
  {
    public sealed override void ExecuteCommand()
    {
      try
      {
        ExecuteCommandImpl();
      } catch(Exception e)
      {
        System.Console.Error.WriteLine("Failed to execute commnad: " + e.Message);
        System.Console.Error.WriteLine(e);
        throw new CommandLineException("TeamCity command failed");
      }
    }

    protected abstract void ExecuteCommandImpl();
  }
}