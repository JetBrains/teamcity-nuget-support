namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class FilterAndTreeNode : FilterTreeNode
  {
    public FilterTreeNode Left { get; private set; }
    public FilterTreeNode Right { get; private set; }

    public FilterAndTreeNode(FilterTreeNode left, FilterTreeNode right)
    {
      Left = left;
      Right = right;
    }

    public override FilterTreeNode Normalize(string propertyName)
    {
      var left = Left.Normalize(propertyName);
      var right = Right.Normalize(propertyName);

      bool leftUnknown = left is FilterUnknownTreeNode;
      bool rightUnknown = right is FilterUnknownTreeNode;

      if (!leftUnknown && !rightUnknown)
        return new FilterAndTreeNode(left, right);
      if (!leftUnknown) return left;
      if (!rightUnknown) return right;

      return base.Normalize(propertyName);
    }

    public override string ToString()
    {
      return string.Format("( {0} ) and ( {1} )", Left, Right);
    }

  }
}