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

    public override string ToString()
    {
      return string.Format("( {0} ) and ( {1} )", Left, Right);
    }

  }
}