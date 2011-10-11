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
  }
}