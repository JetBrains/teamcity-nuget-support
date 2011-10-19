using System.Linq.Expressions;
using System.Reflection;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public class WhereExpressionVisitor<T> : ExpressionVisitor
  {
    [NotNull]
    public FilterTreeNode CheckPropertyEqualExpression(Expression e)
    {
      var propertyCall = IsPropertyCallExpression(e);
      if (propertyCall != null)
      {
        return new FilterEqualsTreeNode(propertyCall, new ConstantValue(true));
      }

      var uExpression = e as UnaryExpression;
      if (uExpression != null && uExpression.NodeType == ExpressionType.Not)
      {
        var node = IsPropertyCallExpression(uExpression.Operand);
        if (node != null) return new FilterEqualsTreeNode(node, new ConstantValue(false));

        var result = CheckPropertyEqualExpression(uExpression.Operand);
        return new FilterNotTreeNode(result);
      }

      var expression = e as BinaryExpression;
      if (expression == null) return new FilterUnknownTreeNode();

      if (expression.NodeType == ExpressionType.Equal)
      {
        var leftValue = EvaluateConstant(expression.Left);
        var rightProp = IsPropertyCallExpression(expression.Right);

        if (leftValue != null && rightProp != null)
        {          
          return new FilterEqualsTreeNode(rightProp, leftValue);
        }

        var rightValue = EvaluateConstant(expression.Right);
        var leftProp = IsPropertyCallExpression(expression.Left);
        if (rightValue != null && leftProp != null)
        {
          return new FilterEqualsTreeNode(leftProp, rightValue);
        }
        return new FilterUnknownTreeNode();
      }
      if (expression.NodeType == ExpressionType.NotEqual)
      {
        var leftValue = EvaluateConstant(expression.Left);
        var rightProp = IsPropertyCallExpression(expression.Right);

        if (leftValue != null && rightProp != null)
        {          
          return new FilterNotTreeNode(new FilterEqualsTreeNode(rightProp, leftValue));
        }

        var rightValue = EvaluateConstant(expression.Right);
        var leftProp = IsPropertyCallExpression(expression.Left);
        if (rightValue != null && leftProp != null)
        {
          return new FilterNotTreeNode(new FilterEqualsTreeNode(leftProp, rightValue));
        }
        return new FilterUnknownTreeNode();
      }

      if (expression.NodeType == ExpressionType.Or || expression.NodeType == ExpressionType.OrElse)
      {
        var left = CheckPropertyEqualExpression(expression.Left);
        var right = CheckPropertyEqualExpression(expression.Right);
        return new FilterOrTreeNode(left, right);
      }

      if (expression.NodeType == ExpressionType.Add || expression.NodeType == ExpressionType.AndAlso)
      {
        var left = CheckPropertyEqualExpression(expression.Left);
        var right = CheckPropertyEqualExpression(expression.Right);
        return new FilterAndTreeNode(left, right);
      }

      return new FilterUnknownTreeNode();
    }

    [CanBeNull]
    private ConstantValue EvaluateConstant(Expression e)
    {
      var ce = e as ConstantExpression;
      if (ce != null)
        return new ConstantValue(ce.Value);

      //TODO: handle nulls
      return null;
    }

    [CanBeNull]
    private PropertyCall IsPropertyCallExpression(Expression e)
    {
      var ie = e as MethodCallExpression;
      if (ie != null && ie.Method.DeclaringType == typeof(string) && ie.Method.Name == "ToLower")
      {
        var expr = IsPropertyCallExpression(ie.Object);
        if (expr != null)
        {
          return new PropertyCall(expr.PropertyName, true);
        }
      }

      var me = e as MemberExpression;
      if (me == null) return null;      
      if (me.Member.DeclaringType != typeof(T)) return null;
      if (!(me.Member is PropertyInfo)) return null;

      return new PropertyCall(me.Member.Name);
    }
  }
}