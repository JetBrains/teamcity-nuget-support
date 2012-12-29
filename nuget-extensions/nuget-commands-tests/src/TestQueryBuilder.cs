using System.Linq.Expressions;
using NUnit.Framework;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [TestFixture]
  public class TestQueryBuilder
  {
    [Test]
    public void TestIdFilter_0()
    {
      var exp = QueryBuilder.GeneratePackageIdFilter(new string[0], Expression.Parameter(typeof(IPackageMetadata), "p"));
      Assert.AreEqual(exp.ToString(), "True");
    }

    [Test]
    public void TestIdFilter_1()
    {
      var exp = QueryBuilder.GeneratePackageIdFilter(new[] {"a"}, Expression.Parameter(typeof (IPackageMetadata), "p"));
      Assert.AreEqual(exp.ToString(), "(p.Id.ToLower() == \"a\")");
    }

    [Test]
    public void TestIdFilter_2()
    {
      var exp = QueryBuilder.GeneratePackageIdFilter(new[] {"a", "b"}, Expression.Parameter(typeof (IPackageMetadata), "p"));
      Assert.AreEqual(exp.ToString(), "((p.Id.ToLower() == \"a\") Or (p.Id.ToLower() == \"b\"))");
    }

    [Test]
    public void TestIdFilter_3()
    {
      var exp = QueryBuilder.GeneratePackageIdFilter(new[] { "a", "b", "c" }, Expression.Parameter(typeof(IPackageMetadata), "p"));
      Assert.AreEqual(exp.ToString(), "(((p.Id.ToLower() == \"a\") Or (p.Id.ToLower() == \"b\")) Or (p.Id.ToLower() == \"c\"))");
    }

    [Test]
    public void TestIdFilter_4()
    {
      var exp = QueryBuilder.GeneratePackageIdFilter(new[] { "a", "b", "c", "d" }, Expression.Parameter(typeof(IPackageMetadata), "p"));
      Assert.AreEqual(exp.ToString(), "(((p.Id.ToLower() == \"a\") Or (p.Id.ToLower() == \"b\")) Or ((p.Id.ToLower() == \"c\") Or (p.Id.ToLower() == \"d\")))");
    }
  }
}
