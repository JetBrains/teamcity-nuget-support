namespace JetBrains.TeamCity.NuGet.Feed.Query.Tree
{
  public class FilterNotTreeNode : FilterTreeNode
  {
    public FilterTreeNode Operand { get; private set; }

    public FilterNotTreeNode(FilterTreeNode operand)
    {
      Operand = operand;
    }

    public override FilterTreeNode Normalize(string propertyName)
    {
      var not = Operand as FilterNotTreeNode;
      if (not != null) 
        return not.Operand.Normalize(propertyName);

      var negOp = Negatiate(Operand).Normalize(propertyName);
      if (!(negOp is FilterUnknownTreeNode)) 
        return negOp;


      var op = Operand.Normalize(propertyName);
      if (op is FilterUnknownTreeNode)
        return new FilterUnknownTreeNode();

      return new FilterNotTreeNode(op);
    }

    private static FilterTreeNode Negatiate(FilterTreeNode node)
    {
      if (node is FilterUnknownTreeNode) return node;

      var ex = node as FilterNotTreeNode;
      if (ex != null) 
        return ex.Operand;

      var or = node as FilterOrTreeNode;
      if (or != null)
        return new FilterAndTreeNode(Negatiate(or.Left), Negatiate(or.Right));

      var and = node as FilterAndTreeNode;
      if (and != null)
        return new FilterOrTreeNode(Negatiate(and.Left), Negatiate(and.Right));

      return new FilterUnknownTreeNode();
    }

    public override string ToString()
    {
      return "not ( " + Operand + " )";
    }
  }
}