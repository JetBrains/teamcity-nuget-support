using JetBrains.TeamCity.NuGet.Feed.Query.Tree;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Feed
{
  [TestFixture]
  public class QueryNormalizationTest
  {

    [Test]
    public void test_equals_is_normal()
    {
      var expr = eq("A", 1);
      DoNormalizeTest(expr, "A", "$.A == 1");
      DoNormalizeTest(expr, "B", "???");
    }

    [Test]
    public void test_not_eq()
    {
      var expr = not(eq("A", 1));
      DoNormalizeTest(expr, "A", "not ( $.A == 1 )");
    }

    [Test]
    public void test_not_not_eq()
    {
      var expr = not(not(eq("A", 1)));
      DoNormalizeTest(expr, "A", "$.A == 1");
    }

    [Test]
    public void test_not_or_not()
    {
      // !( !a or !b) => a and b
      var expr = not(or(not(eq("A", 1)), not(eq("B", 2))));
      DoNormalizeTest(expr, "A", "$.A == 1");
      DoNormalizeTest(expr, "B", "$.B == 2");
    }

    [Test]
    public void test_not_and_not()
    {
      //  !( !a and !b) => a or b
      var expr = not(and(not(eq("A", 1)), not(eq("A", 2))));
      DoNormalizeTest(expr, "A", "( $.A == 1 ) or ( $.A == 2 )");
    }

    [Test]
    public void test_not_and_not_or()
    {
      //  d or c or !( !a and !b) or e 
      // 
      var expr = or(or(eq("A", 3), eq("A", 2)), not(and(not(eq("A", 1)), not(eq("A", 2)))));
      DoNormalizeTest(expr, "A", "( ( $.A == 3 ) or ( $.A == 2 ) ) or ( ( $.A == 1 ) or ( $.A == 2 ) )");
      
    }

    [Test]
    public void test_normalize_and()
    {
      DoNormalizeTest(and(eq("A", 1), eq("A", 1)), "A", "( $.A == 1 ) and ( $.A == 1 )");
    }

    [Test]
    public void test_normalize_ands()
    {
      var expr = and(eq("A", 1), eq("B", 2));

      DoNormalizeTest(expr, "A", "$.A == 1");
      DoNormalizeTest(expr, "B", "$.B == 2");
      DoNormalizeTest(expr, "Q", "???");
    }

    [Test]
    public void test_normalize_ors()
    {
      var expr = or(eq("A", 1), eq("B", 2));
      DoNormalizeTest(expr, "A", "???");
      DoNormalizeTest(expr, "B", "???");
      DoNormalizeTest(expr, "Q", "???");
    }

    [Test]
    public void test_several_ands()
    {
      var expr = and(and(eq("A", 1), eq("B", 2)), eq("Q", 1));
      DoNormalizeTest(expr, "A", "$.A == 1");
      DoNormalizeTest(expr, "B", "$.B == 2");
      DoNormalizeTest(expr, "Q", "$.Q == 1");
    }

    [Test]
    public void test_and_unknwons()
    {
      DoNormalizeTest(and(unknown(), eq("A", 1)), "A", "$.A == 1");
    }

    [Test]
    public void test_or_unknwons()
    {
      DoNormalizeTest(or(unknown(), eq("A", 1)), "A", "???");
    }

    [Test]
    public void test_ors()
    {
      var expr = and(eq("Q", 1), or(eq("A", 2), eq("A", 3)));
      DoNormalizeTest(expr, "A", "( $.A == 2 ) or ( $.A == 3 )");
      DoNormalizeTest(expr, "Q", "$.Q == 1");
    }

    [Test]
    public void test_or_and()
    {
      //test: (a & b) or (a & c) => a & (b or c)
      var expr = or(and(eq("A", 1), eq("B", 1)), and(eq("A", 1), eq("C",5)));
      DoNormalizeTest(expr, "A", "( $.A == 1 ) or ( $.A == 1 )");
      DoNormalizeTest(expr, "B", "???");
      DoNormalizeTest(expr, "C", "???");
    }

    [Test]
    public void test_ors2()
    {
      var expr = or(eq("A", 1), or(eq("A", 2), eq("A", 3)));
      DoNormalizeTest(expr, "A", "( $.A == 1 ) or ( ( $.A == 2 ) or ( $.A == 3 ) )");
    }

    private void DoNormalizeTest(FilterTreeNode node, string propertyName, string result)
    {
      Assert.AreEqual(result.Trim(), node.Normalize(propertyName).ToString().Trim());
    }

    private FilterTreeNode unknown()
    {
      return new FilterUnknownTreeNode();
    }

    private FilterTreeNode or(FilterTreeNode a, FilterTreeNode b)
    {
      return new FilterOrTreeNode(a, b);
    }

    private FilterTreeNode and(FilterTreeNode a, FilterTreeNode b)
    {
      return new FilterAndTreeNode(a, b);
    }

    private FilterTreeNode not(FilterTreeNode a)
    {
      return new FilterNotTreeNode(a);
    }

    private FilterTreeNode eq(string a, object b)
    {
      return new FilterEqualsTreeNode(new PropertyCall(a), new ConstantValue(b));
    }

  }
}