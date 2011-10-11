using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class TCQueryable<T> : IQueryable<T>
  {
    private readonly TCQueryProvider myProvider;
    private readonly Expression myExpression;

    public TCQueryable(TCQueryProvider provider, Expression expression)
    {
      myProvider = provider;
      myExpression = expression;
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
      return GetEnumerator();
    }

    public IEnumerator<T> GetEnumerator()
    {
      return Provider.Execute<IEnumerable<T>>(Expression).GetEnumerator();
    }

    public Expression Expression
    {
      get { return myExpression; }
    }

    public Type ElementType
    {
      get { return typeof(T); }
    }

    IQueryProvider IQueryable.Provider
    {
      get { return Provider; }
    }

    public TCQueryProvider Provider
    {
      get { return myProvider; }
    }
  }
}