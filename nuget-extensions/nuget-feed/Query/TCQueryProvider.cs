using System;
using System.Linq;
using System.Linq.Expressions;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public abstract class TCQueryProvider : IQueryProvider
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    public IQueryable CreateQuery(Expression expression)
    {
      LOG.InfoFormat("Create query: {0} of {1}", expression, expression.Type);
      
      var type = TypeInference.FindIEnumerable(expression.Type);
      var genericType = typeof (TCQueryable<>).MakeGenericType(type);
      return (IQueryable) Activator.CreateInstance(genericType, this, expression);
    }

    public virtual IQueryable<TElement> CreateQuery<TElement>(Expression expression)
    {
      return new TCQueryable<TElement>(this, expression);
    }

    public abstract object Execute(Expression expression);

    public abstract TResult Execute<TResult>(Expression expression);  
  }
}