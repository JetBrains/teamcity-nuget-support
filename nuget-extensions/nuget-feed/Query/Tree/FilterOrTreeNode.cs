namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class FilterOrTreeNode : FilterTreeNode
  {
    public FilterTreeNode Left { get; private set; }
    public FilterTreeNode Right { get; private set; }

    public FilterOrTreeNode(FilterTreeNode left, FilterTreeNode right)
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
        return new FilterOrTreeNode(left, right);

      return base.Normalize(propertyName);
    }

    public override string ToString()
    {
      return string.Format("( {0} ) or ( {1} )", Left, Right);
    }
  }
}