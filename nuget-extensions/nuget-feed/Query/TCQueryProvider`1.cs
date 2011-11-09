using System;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class TCQueryProvider<T> : TCQueryProvider
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly IQueryable<T> myProvider;

    public TCQueryProvider(IQueryable<T> provider)
    {
      myProvider = provider;
    }

    public override IQueryable<TElement> CreateQuery<TElement>(Expression expression)
    {
      LOG.InfoFormat("Create query: {0} of {1}", expression, expression.Type);

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
            var result = OptimizeWhereQuery<TElement>(lambda, tree);
            if (result != null)
              return result;
          }
        }
      }
      
      return base.CreateQuery<TElement>(expression);
    }

    protected virtual IQueryable<TElement> OptimizeWhereQuery<TElement>(LambdaExpression expression, FilterTreeNode tree)
    {
      if (typeof(TElement) == typeof(T))
      {
        return (IQueryable<TElement>) OptimizeBasicWhereQuery((Expression<Func<T, bool>>) expression, tree);
      }
      return null;
    }

    protected virtual IQueryable<T> OptimizeBasicWhereQuery(Expression<Func<T, bool>> expression, FilterTreeNode node)
    {
      return null;
    } 

    public override object Execute(Expression expression)
    {
      LOG.InfoFormat("Execute expression: {0}", expression);
      return myProvider.Provider.Execute(expression);
    }

    public override TResult Execute<TResult>(Expression expression)
    {
      LOG.InfoFormat("Execute expression: {0}", expression);
      return myProvider.Provider.Execute<TResult>(expression);
    }

    public IQueryable<T> Query
    {
      get { return new TCQueryable<T>(this, Expression.Constant(myProvider));}
    }
  }
}