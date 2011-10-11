using System;
using System.Linq;
using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class TCQueryProvider<T> : TCQueryProvider
  {
    private readonly IQueryable<T> myProvider;

    public TCQueryProvider(IQueryable<T> provider)
    {
      myProvider = provider;
    }

    public override IQueryable<TElement> CreateQuery<TElement>(Expression expression)
    {
      var callExpression = expression as MethodCallExpression;
      if (callExpression != null && callExpression.Method.DeclaringType == typeof(Queryable))
      {
        var whereCall = InnermostWhereFinder.InnermostWhere(callExpression);
        if (whereCall != null)
        {
          var lambda = UpperMostLambdaFinder.UpperMostLambda(whereCall);
          if (lambda != null)
          {
            new WhereExpressionVisitor<T>().CheckPropertyEqualExpression(lambda.Body, "A", val => Console.Out.WriteLine("Found expression: {0}", val));
          }
        }
      }
      
      return base.CreateQuery<TElement>(expression);
    }

    public override object Execute(Expression expression)
    {
      return myProvider.Provider.Execute(expression);
    }

    public override TResult Execute<TResult>(Expression expression)
    {
      Console.Out.WriteLine("expression = {0}", expression);
      return myProvider.Provider.Execute<TResult>(expression);
    }

    public IQueryable<T> Query
    {
      get { return new TCQueryable<T>(this, Expression.Constant(myProvider));}
    }
  }
}