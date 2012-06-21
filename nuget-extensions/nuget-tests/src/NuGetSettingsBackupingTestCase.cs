using System;
using System.IO;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class NuGetSettingsBackupingTestCase
  {
    private String myNuGetConfigBackup;

    protected string NuGetConfigPath
    {
      get
      {
        string appDataPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        return Path.Combine(appDataPath, "NuGet", "NuGet.Config");
      }
    }

    [SetUp]
    public virtual void SetUp()
    {
      myNuGetConfigBackup = null;
      if (File.Exists(NuGetConfigPath))
      {
        myNuGetConfigBackup = NuGetConfigPath + ".TeamCityTest";
        SafeRemove(myNuGetConfigBackup);
        File.Copy(NuGetConfigPath, myNuGetConfigBackup);
      }
    }

    private static void SafeRemove(string nuGetConfigBackup)
    {
      if (!File.Exists(nuGetConfigBackup)) return;
      File.SetAttributes(nuGetConfigBackup, FileAttributes.Normal);
      File.Delete(nuGetConfigBackup);
    }

    [TearDown]
    public virtual void TearDown()
    {
      if (myNuGetConfigBackup == null) return;
      
      if (File.Exists(myNuGetConfigBackup))
      {
        SafeRemove(NuGetConfigPath);
        File.Copy(myNuGetConfigBackup, NuGetConfigPath);
      }
    }
  }
}