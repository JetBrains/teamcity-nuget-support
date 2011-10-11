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

    public override string ToString()
    {
      return string.Format("( {0} ) or ( {1} )", Left, Right);
    }
  }
}