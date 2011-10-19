using System.Linq;
using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class InnermostWhereFinder : ExpressionVisitor
  {
    private MethodCallExpression myWhere;

    public static MethodCallExpression InnermostWhere(Expression expression)
    {
      var finder = new InnermostWhereFinder();
      finder.Visit(expression);
      return finder.myWhere;
    }

    protected override Expression VisitMethodCall(MethodCallExpression expression)
    {
      if (expression.Method.Name == "Where" && expression.Method.DeclaringType == typeof(Queryable))
      {
        myWhere = expression;
      }

      Visit(expression.Arguments[0]);
      return expression;
    }
  }
}