using System;
using System.Collections.Generic;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed
{
  [TestFixture]
  public class TCQueryableTest
  {
    private readonly IEnumerable<Obj> myOInput = new[]
                                                   {
                                                     new Obj {A = 2, B = "a"}, 
                                                     new Obj {A = 3, B = "zz"},
                                                     new Obj {A = 11, B = "14"}
                                                   };

    private TestTCQueryProvider<Obj> myOProv; 
    private IQueryable<Obj> myOQuery;
      
    [SetUp]
    public void SetUp()
    {
      myOProv = new TestTCQueryProvider<Obj>(myOInput.AsQueryable());
      myOQuery = myOProv.Query;
    }

    [Test]
    public void Test_OWhere_eq()
    {
      Call(myOQuery.Where(x => x.A == 1), "$.A == 1");
    }

    [Test]
    public void Test_OWhere_eq_toLover()
    {
      Call(myOQuery.Where(x => x.B.ToLower() == "q"), "$.B =L= q");
    }

    [Test]
    public void Test_OWhere_not_eq()
    {
      Call(myOQuery.Where(x => !(x.A == 1)), "not ( $.A == 1 )");
    }
    [Test]
    public void Test_OWhere_not_eq2()
    {
      Call(myOQuery.Where(x => x.A != 1), "not ( $.A == 1 )");
    }

    [Test]
    public void Test_OWhere_eq_flip()
    {
      Call(myOQuery.Where(x => 1 == x.A), "$.A == 1");
    }

    [Test]
    public void Test_OWhere_open_bool()
    {
      Call(myOQuery.Where(x => x.C), "$.C == True");
    }

    [Test]
    public void Test_OWhere_open_not_bool()
    {
      Call(myOQuery.Where(x => !x.C), "$.C == False");
    }

    [Test]
    public void Test_OWhere_eq2()
    {
      Call(myOQuery.Where(x => x.B == "Zzz"), "$.B == Zzz");
    }

    [Test]
    public void Test_OWhere_eq_or_eq()
    {
      Call(myOQuery.Where(x => x.A == 1 || x.A == 2), "( $.A == 1 ) or ( $.A == 2 )");
    }

    [Test]
    public void Test_OWhere_eq_or_eq2()
    {
      Call(myOQuery.Where(x => x.A == 1 || x.B == "Q"), "( $.A == 1 ) or ( $.B == Q )");
    }

    [Test]
    public void Test_OWhere_eq_and_eq()
    {
      Call(myOQuery.Where(x => x.A == 1 && x.A == 2), "( $.A == 1 ) and ( $.A == 2 )");
    }

    [Test]
    public void Test_OWhere_eq_and_eq2()
    {
      Call(myOQuery.Where(x => x.A == 1 && x.B == "W"), "( $.A == 1 ) and ( $.B == W )");
    }

    [Test]
    public void Test_OWhere_greather_no_tree()
    {
      Call(myOQuery.Where(x => x.A > 3), "???");
    }

    [Test]
    public void Test_OWhere_open_bool_or()
    {
      Call(myOQuery.Where(x => x.C || x.A == 1), "( $.C == True ) or ( $.A == 1 )");
    }

    [Test]
    public void Test_OWhere_bool_and_lower()
    {
      Call(myOQuery.Where(x => x.C && x.A < 1), "( $.C == True ) and ( ??? )");
    }

    [Test]
    public void Test_OWhere_bool_or_lower()
    {
      Call(myOQuery.Where(x => x.C || x.A < 1), "( $.C == True ) or ( ??? )");
    }

    private void Call<T>(IEnumerable<T> query, string gold)
    {
      foreach (var t in query)
      {
        Console.Out.WriteLine("Value: " + t);
      }
      
      if (gold != null)
      {
        Assert.IsTrue(myOProv.Trees.Count == 1);
        Assert.AreEqual(gold.Trim(), myOProv.Trees.Values.Single().ToString().Trim());
      } else
      {
        Assert.IsTrue(myOProv.Trees.Count == 0);
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