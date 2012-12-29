using System;
using JetBrains.TeamCity.NuGetRunner;
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
        new AssemblyResolver(GetType().Assembly.GetAssemblyDirectory());
      } catch(Exception e)
      {
        System.Console.Error.WriteLine("Failed to set assembly resolver: " + e.Message);
        System.Console.Error.WriteLine(e);
      }
      try
      {
        ExecuteCommandImpl();
      } catch(Exception e)
      {
        System.Console.Error.WriteLine("Failed to execute command: " + e.Message);
        System.Console.Error.WriteLine(e);
        throw new CommandLineException("TeamCity command failed");
      }
    }

    protected abstract void ExecuteCommandImpl();
  }
}