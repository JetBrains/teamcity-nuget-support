using System;
using System.Collections.Generic;
using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class WhereExpressionVisitor<T> : ExpressionVisitor
  {
    private readonly object myValue;
    private readonly Dictionary<string, List<string>> myFilters = new Dictionary<string, List<string>>();
    private bool myIsSupported = true;

    public WhereExpressionVisitor(object value)
    {
      myValue = value;
    }

    public Dictionary<string, List<string>> Filters
    {
      get { return myFilters; }
    }

    private void unsupportedExpression()
    {
      myIsSupported = false;
    }

    protected override Expression VisitLambda<T1>(Expression<T1> node)
    {
      Console.Out.WriteLine("Visit Lambda");
      return base.VisitLambda(node);
    }

    protected override Expression VisitUnary(UnaryExpression node)
    {
      Console.Out.WriteLine("Visit unary: {0}", node.Method);
      return base.VisitUnary(node);
    }

    protected override Expression VisitBinary(BinaryExpression node)
    {
      Console.Out.WriteLine("Visit binary: {0}", node.Method);
      return base.VisitBinary(node);
    }

    protected override Expression VisitParameter(ParameterExpression node)
    {
      Console.Out.WriteLine("Visit parameter");
      return base.VisitParameter(node);
    }

    protected override Expression VisitConstant(ConstantExpression node)
    {
      Console.Out.WriteLine("Visit constant: {0}", node.Value ?? "<null>");
      return base.VisitConstant(node);
    }

    protected override Expression VisitInvocation(InvocationExpression node)
    {
      Console.Out.WriteLine("Visist invocation");
      unsupportedExpression();
      return base.VisitInvocation(node);
    }

    protected override Expression VisitLoop(LoopExpression node)
    {
      Console.Out.WriteLine("Loop expression");
      unsupportedExpression();
      return base.VisitLoop(node);
    }

    protected override Expression VisitMember(MemberExpression node)
    {
      Console.Out.WriteLine("Visit member: {0}", node.Member);
      return base.VisitMember(node);
    }
  }
}