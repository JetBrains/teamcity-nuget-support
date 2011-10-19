using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.Feed.Query;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class TestTCQueryProvider<T> : TCQueryProvider<T>
  {
    private readonly Dictionary<Expression, FilterTreeNode> myTrees = new Dictionary<Expression, FilterTreeNode>();

    public TestTCQueryProvider(IQueryable<T> provider)
      : base(provider)
    {
    }

    protected override IQueryable<TElement> OptimizeWhereQuery<TElement>(LambdaExpression expression, FilterTreeNode tree)
    {
      myTrees[expression] = tree;
      Console.Out.WriteLine("Optimize expression: {0}", expression);
      Console.Out.WriteLine("AST : {0}", tree);

      return base.OptimizeWhereQuery<TElement>(expression, tree);
    }

    public Dictionary<Expression, FilterTreeNode> Trees
    {
      get { return myTrees; }
    }
  }
}