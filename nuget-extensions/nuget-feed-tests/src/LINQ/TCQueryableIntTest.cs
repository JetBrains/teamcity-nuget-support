using System;
using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Feed.Tests.LINQ
{
  [TestFixture]
  public class TCQueryableIntTest
  {
    private readonly IEnumerable<int> myInput = new[] { 1, 2, 3, 4, 5, 6 };
    private IQueryable<int> myQuery;

    [SetUp]
    public void SetUp()
    {
      myQuery = new TestTCQueryProvider<int>(myInput.AsQueryable()).Query;
    }

    [Test]
    public void Test_Where_Greather()
    {
      Call(myQuery.Where(x => x > 3));
    }

    private void Call<T>(IEnumerable<T> query)
    {
      foreach (var t in query)
      {
        Console.Out.WriteLine("Value: " + t);
      }
    }

  }
}