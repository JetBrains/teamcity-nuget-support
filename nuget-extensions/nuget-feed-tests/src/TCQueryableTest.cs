using System;
using System.Collections.Generic;
using JetBrains.TeamCity.NuGet.Feed.Query;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed
{
  [TestFixture]
  public class TCQueryableTest
  {
    private readonly IEnumerable<int> myInput = new[] {1, 2, 3, 4, 5, 6};

    private readonly IEnumerable<Obj> myOInput = new[]
                                                   {
                                                     new Obj {A = 2, B = "a"}, 
                                                     new Obj {A = 3, B = "zz"},
                                                     new Obj {A = 11, B = "14"}
                                                   };
    private IQueryable<int> myQuery;
    private IQueryable<Obj> myOQuery;
      
    [SetUp]
    public void SetUp()
    {
      myQuery = new TCQueryProvider<int>(myInput.AsQueryable()).Query;
      myOQuery = new TCQueryProvider<Obj>(myOInput.AsQueryable()).Query;
    }

    [Test]
    public void Test_Where()
    {
      Call(myQuery.Where(x => x > 3));
    }

    [Test]
    public void Test_OWhere_PropertyEquals()
    {
      Call(myOQuery.Where(x => x.A == 1));
    }

    [Test]
    public void Test_OWhere_PropertyEqualsOrPropertyEquals()
    {
      Call(myOQuery.Where(x => x.A == 1 || x.A == 2));
    }

    [Test]
    public void Test_OWhere()
    {
      Call(myOQuery.Where(x => x.A > 3));
    }

    [Test]
    public void Test_OWhere2()
    {
      Call(myOQuery.Where(x => x.A > 3));
    }

    [Test]
    public void Test_OWhere3()
    {
      Call(myOQuery.Where(x => x.C));
    }

    [Test]
    public void Test_OWhere_And()
    {
      Call(myOQuery.Where(x => x.C && x.A < 1));
    }

    [Test]
    public void Test_OWhere_Or()
    {
      Call(myOQuery.Where(x => x.C || x.A < 1));
    }

    private void Call<T>(IEnumerable<T> query)
    {
      foreach (var t in query)
      {
        Console.Out.WriteLine("Value: " + t);
      }
    }

    public class Obj
    {
      public int A { get; set; }
      public string B { get; set; }
      public bool C { get; set; }
    }
     
  }
}