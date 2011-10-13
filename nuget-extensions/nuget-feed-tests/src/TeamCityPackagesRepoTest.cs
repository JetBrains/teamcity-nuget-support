using System;
using System.Text;
using System.Threading;
using JetBrains.TeamCity.NuGet.Feed.DataServices;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  [TestFixture]
  public class TeamCityPackagesRepoTest
  {
    private TeamCityPackagesRepo myRepo;
    private StringBuilder myTrash;

    [SetUp]
    public void SetUp()
    {
      myRepo = new TeamCityPackagesRepo();
      myTrash = new StringBuilder();      
    }


    [Test]
    public void ConcurrentTest()
    {
      DoInThreads(5, 150, 
        () => Read(myRepo.Specs),
        () => myRepo.RemoveSpecs(new TeamCityPackageEntry[0]),
        () => myRepo.AddSpec(555.ToEntry())
        );
    }

    private void Read(object o)
    {
      myTrash.Append(o);
    }

    private void DoInThreads(int count, int repeat, params Action[] tasks)
    {
      var threads = count.Repeat(() => tasks.Select(x => new Thread(() => { for (int i = 0; i < repeat; i++) x(); }))).SelectMany(x => x).ToList();
      foreach (var thread in threads)
      {
        thread.Start();
      }

      foreach (var thread in threads)
      {
        thread.Join();
      }
    }
  }
}