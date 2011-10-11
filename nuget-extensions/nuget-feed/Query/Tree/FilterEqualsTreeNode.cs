using System;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class PropertyCall
  {
    public string PropertyName { get; private set; }
    public bool CallToLower { get; private set; }

    public PropertyCall(string propertyName)
    {
      PropertyName = propertyName;
    }

    public PropertyCall(string propertyName, bool callToLower)
    {
      PropertyName = propertyName;
      CallToLower = callToLower;
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
    public bool ToLower { get; private set; }
    public String FieldName { get; private set; }
    public object Value { get; private set; }

    public FilterEqualsTreeNode([NotNull] PropertyCall fieldName, [NotNull] ConstantValue value)
    {
      FieldName = fieldName.PropertyName;
      ToLower = fieldName.CallToLower;
      Value = value.Value;
    }

    public override string ToString()
    {
      if (!ToLower) 
      return string.Format("$.{0} == {1}", FieldName, Value);
      else
      return string.Format("$.{0} =L= {1}", FieldName, Value);
    }
  }
}