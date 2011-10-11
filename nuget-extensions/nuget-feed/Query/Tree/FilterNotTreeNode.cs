namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class FilterNotTreeNode : FilterTreeNode
  {
    public FilterTreeNode Operand { get; private set; }

    public FilterNotTreeNode(FilterTreeNode operand)
    {
      Operand = operand;
    }
  }
}