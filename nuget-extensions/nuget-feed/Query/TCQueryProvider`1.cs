using System;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;

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
            var tree = new WhereExpressionVisitor<T>().CheckPropertyEqualExpression(lambda.Body);            
            if (tree != null)
            {
              var result = OptimizeWhereQuery<TElement>(expression, tree);
              if (result != null)
              {
                return result;
              }
            }
          }
        }
      }
      
      return base.CreateQuery<TElement>(expression);
    }

    protected virtual IQueryable<TElement> OptimizeWhereQuery<TElement>(Expression expression, FilterTreeNode tree)
    {
      return null;
    }

    public override object Execute(Expression expression)
    {
      return myProvider.Provider.Execute(expression);
    }

    public override TResult Execute<TResult>(Expression expression)
    {
      return myProvider.Provider.Execute<TResult>(expression);
    }

    public IQueryable<T> Query
    {
      get { return new TCQueryable<T>(this, Expression.Constant(myProvider));}
    }
  }
}