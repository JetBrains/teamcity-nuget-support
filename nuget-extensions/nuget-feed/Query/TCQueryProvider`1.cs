using System;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class TCQueryProvider<T> : TCQueryProvider
  {
    private readonly IQueryable<T> myProvider;

    public TCQueryProvider(IQueryable<T> provider)
    {
      myProvider = provider;
    }

    private bool CheckPropertyEqualExpression(Expression e, string propertyName, Action<object> valueFound)
    {
      if (IsPropertyCallExpression(e, propertyName))
      {
        valueFound(true);
        return true;
      }

      var uExpression = e as UnaryExpression;
      if (uExpression != null && uExpression.NodeType == ExpressionType.Not && IsPropertyCallExpression(uExpression.Operand, propertyName))
      {
        valueFound(false);
        return true;
      }

      var expression = e as BinaryExpression;
      if (expression == null) return false;

      if (expression.NodeType == ExpressionType.Equal)
      {
        var leftValue = EvaluateConstant(expression.Left);
        var rightValue = EvaluateConstant(expression.Right);

        if (leftValue != null && IsPropertyCallExpression(expression.Right, propertyName))
        {
          valueFound(leftValue);
          return true;
        }

        if (rightValue != null && IsPropertyCallExpression(expression.Left, propertyName))
        {
          valueFound(rightValue);
          return true;
        }
        return false;
      }

      if (expression.NodeType == ExpressionType.Or || expression.NodeType == ExpressionType.OrElse)
      {
        return
          CheckPropertyEqualExpression(expression.Left, propertyName, valueFound)
          &&
          CheckPropertyEqualExpression(expression.Right, propertyName, valueFound)
          ;
      }

      return false;
    }

    private object EvaluateConstant(Expression e)
    {
      var ce = e as ConstantExpression;
      if (ce != null)
        return ce.Value;

      //TODO: handle nulls
      return null;
    }

    private bool IsPropertyCallExpression(Expression e, string propertyName)
    {
      var me = e as MemberExpression;
      if (me == null) return false;
      if (me.Member.Name != propertyName) return false;
      if (me.Member.DeclaringType != typeof(T)) return false;
      return me.Member is PropertyInfo;
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
            CheckPropertyEqualExpression(lambda.Body, "A", val => Console.Out.WriteLine("Found expression: {0}", val));
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