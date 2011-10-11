using System;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class PropertyCall
  {
    public string PropertyName { get; private set; }

    public PropertyCall(string propertyName)
    {
      PropertyName = propertyName;
    }
  }

  public class ConstantValue
  {
    public object Value { get; private set; }

    public ConstantValue(object value)
    {
      Value = value;
    }
  }

  public class FilterEqualsTreeNode : FilterTreeNode
  {
    public String FieldName { get; private set; }
    public object Value { get; private set; }

    public FilterEqualsTreeNode([NotNull] PropertyCall fieldName, [NotNull] ConstantValue value)
    {
      FieldName = fieldName.PropertyName;
      Value = value.Value;
    }
  }
}