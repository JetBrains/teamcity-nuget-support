using System;
using NuGet;
using NuGet.CommandLine;
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
        System.Console.Error.WriteLine("Failed to execute command: " + e.Message);
        System.Console.Error.WriteLine(e);
        throw new CommandException("TeamCity command failed");
      }
    }

    protected abstract void ExecuteCommandImpl();
  }
}
