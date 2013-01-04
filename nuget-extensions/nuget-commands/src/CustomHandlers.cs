using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  /// <summary>
  /// Marker interface to make NuGet's MEF instanciate the component
  /// </summary>
  [InheritedExport]
  public interface ICreatableComponent
  {
    void Initialize();
    String Describe();
  }

  [Command("TeamCity.NOOP", "Command that does nothing, but creates necessary MEF components")]
  public class CustomHandlers : CommandBase, IPartImportsSatisfiedNotification
  {
    private readonly IEnumerable<ICreatableComponent> myComponents;

    [ImportingConstructor]
    public CustomHandlers([ImportMany] IEnumerable<ICreatableComponent> components)
    {
      myComponents = components;
    }

    public void OnImportsSatisfied()
    {
      foreach (var comp in myComponents)
      {
        try
        {
          comp.Initialize();
        }
        catch (Exception e)
        {
          System.Console.Out.WriteLine("Failed to complete initialization of {0}. {1}", comp.GetType().FullName, e.Message);
          System.Console.Out.WriteLine(e);
        }
      }
    }

    protected override void ExecuteCommandImpl()
    {
      foreach (var component in myComponents)
      {
        System.Console.Out.WriteLine("Component: {0}. {1}", component.GetType().FullName, component.Describe());
      }
      System.Console.Out.WriteLine("NOOP Command completed.");
    }    
  }
  
  public class MockCreatableComponent : ICreatableComponent
  {
    public void Initialize()
    {      
    }

    public string Describe()
    {
      return "";
    }
  }
}
