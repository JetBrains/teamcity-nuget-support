namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public abstract class FilterTreeNode
  {
    public virtual FilterTreeNode Normalize()
    {
      return this;
    }
  }
}
