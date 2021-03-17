using System;
using System.Security;
using NUnit.Framework;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.Tests
{
    [TestFixture]
    internal sealed class AssemblyStrongNameHelperTest
    {
      [Test]
      public void ShouldVerifyAssemblyStrongName()
      {
        // Given
        TestDelegate action = () => { AssemblyStrongNameHelper.VerifyStrongName(Files.NuGetExe_5_8); };

        // When
        // Than
        Assert.DoesNotThrow(action);
      }

    [Test]
      public void ShouldNotVerifyAssemblyWrongStrongName()
      {
        // Given
        TestDelegate action = () => { AssemblyStrongNameHelper.VerifyStrongName(Files.WrongAssemblyStrongNameNuGet); };

        // When
        // Than
        Assert.Throws<SecurityException>(action);
      }
    }
}
