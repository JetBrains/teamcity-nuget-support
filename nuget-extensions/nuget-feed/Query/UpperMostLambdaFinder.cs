using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class UpperMostLambdaFinder : ExpressionVisitor
  {
    private LambdaExpression myLambda;

    protected override Expression VisitLambda<T>(Expression<T> node)
    {
      if (myLambda == null)
        myLambda = node;

      return base.VisitLambda(node);
    }



    public static LambdaExpression UpperMostLambda(Expression e)
    {
      var visitor = new UpperMostLambdaFinder();
      visitor.Visit(e);
      return visitor.myLambda;
    }
  }
}