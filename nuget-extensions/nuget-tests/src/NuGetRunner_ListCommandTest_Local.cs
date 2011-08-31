using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListCommandTest_Local_1_4 : NuGetRunner_ListCommandTest_Local_Base
  {
    protected override string NuGetExe
    {
      get { return Files.NuGetExe_1_4; }
    }
  }
  
  [TestFixture]
  public class NuGetRunner_ListCommandTest_Local_1_5 : NuGetRunner_ListCommandTest_Local_Base
  {
    protected override string NuGetExe
    {
      get { return Files.NuGetExe_1_5; }
    }
  }
}