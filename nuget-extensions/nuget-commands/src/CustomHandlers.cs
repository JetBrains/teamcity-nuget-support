using System.Collections.Generic;
using System.ComponentModel.Composition;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  /// <summary>
  /// Marker interface to make NuGet's MEF instanciate the component
  /// </summary>
  public interface ICreatableComponent
  {
    void Initialize();
  }

  public class CustomHandlers : CommandBase
  {
    [ImportingConstructor]
    public CustomHandlers(IEnumerable<ICreatableComponent> components)
    {
      foreach (var comp in components)
      {
        comp.Initialize();
      }
    }

    protected override void ExecuteCommandImpl()
    {
      System.Console.Out.WriteLine("Credentials command");
    }    
  }

  [Export]
  public class MockCreatableComponent : ICreatableComponent
  {
    public void Initialize()
    {
      throw new System.NotImplementedException();
    }
  }
}
