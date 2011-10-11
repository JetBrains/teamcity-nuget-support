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
      DoNormalizeTest(eq("A", 1), "A", "$.A == 1");
      DoNormalizeTest(eq("A", 1), "B", "???");
    }

    [Test]
    public void test_normalize_ands()
    {
      DoNormalizeTest(and(eq("A", 1), eq("B", 2)), "A", "$.A == 1");
      DoNormalizeTest(and(eq("A", 1), eq("B", 2)), "B", "$.B == 1");
      DoNormalizeTest(and(eq("A", 1), eq("B", 2)), "Q", "???");
    }

    [Test]
    public void test_normalize_ors()
    {
      DoNormalizeTest(or(eq("A", 1), eq("B", 2)), "A", "???");
      DoNormalizeTest(and(eq("A", 1), eq("B", 2)), "B", "???");
      DoNormalizeTest(and(eq("A", 1), eq("B", 2)), "Q", "???");
    }

    [Test]
    public void test_several_ands()
    {
      DoNormalizeTest(and(and(eq("A", 1), eq("B", 2)), eq("Q", 1)), "A", "$.A == 1");
      DoNormalizeTest(and(and(eq("A", 1), eq("B", 2)), eq("Q", 1)), "B", "$.B == 2");
      DoNormalizeTest(and(and(eq("A", 1), eq("B", 2)), eq("Q", 1)), "Q", "$.Q == 1");
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
      DoNormalizeTest(and(eq("Q", 1), or(eq("A", 2), eq("A", 3))), "A", "( $.A == 2 ) or ( $.A == 3 )");
      DoNormalizeTest(and(eq("Q", 1), or(eq("A", 2), eq("A", 3))), "Q", "$.Q == 1");
    }

    [Test]
    public void test_or_and()
    {
      //test: (a & b) or (a & c) => a & (b or c)
      DoNormalizeTest(or(and(eq("A", 1), eq("B", 1)), and(eq("A", 1), eq("C",5))), "A", "$.A == 1");
      DoNormalizeTest(or(and(eq("A", 1), eq("B", 1)), and(eq("A", 1), eq("C",5))), "B", "???");
      DoNormalizeTest(or(and(eq("A", 1), eq("B", 1)), and(eq("A", 1), eq("C",5))), "C", "???");
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