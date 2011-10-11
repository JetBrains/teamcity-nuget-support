namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public abstract class FilterTreeNode
  {
    /// <summary>
    /// Returns a one parameter predicate of propertyName. 
    /// It may return FilterUnknownTreeNode if there is no chance to created such function.
    /// 
    /// Let say original expression is Exp, and resulting is Sub,
    /// if sub is not a UnknwownTreeNode, it should apply :
    ///   
    ///   if not sub(a) == true => not Exp,
    /// 
    /// In practice that means: If sub(a) is not true there is no such item in the that may still satisfy Exp.
    /// This would helps us to use one variable index. 
    /// 
    /// </summary>
    /// <param name="propertyName"></param>
    /// <returns></returns>
    public virtual FilterTreeNode Normalize(string propertyName)
    {
      return new FilterUnknownTreeNode();
    }
  }
}
