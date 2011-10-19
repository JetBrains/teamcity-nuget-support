using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class ExpressionTreeModifier<T> : ExpressionVisitor
  {
    private readonly T myObject;

    private ExpressionTreeModifier(T obj)
    {
      myObject = obj;
    }

    protected override Expression VisitConstant(ConstantExpression c)
    {
      // Replace the constant QueryableTerraServerData arg with the queryable Place collection.
      if (c.Type == typeof(T))
        return Expression.Constant(myObject);
      return c;
    }

    public static Expression ReplaceQuery(Expression e, T replacement)
    {
      return new ExpressionTreeModifier<T>(replacement).Visit(e);
    }
  }
}