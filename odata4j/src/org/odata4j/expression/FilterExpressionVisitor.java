package org.odata4j.expression;

import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

public class FilterExpressionVisitor implements ExpressionVisitor {

  // only literals supported, so this suffices for now
  private String fragment;

  private void push(String fragment) {
    this.fragment = fragment;
  }

  @Override
  public String toString() {
    return fragment;
  }

  // literals

  @Override
  public void visit(NullLiteral expr) {
    push("null");
  }

  @Override
  public void visit(BooleanLiteral expr) {
    push(Boolean.toString(expr.getValue()));
  }

  @Override
  public void visit(GuidLiteral expr) {
    push("guid'" + expr.getValue() + "'");
  }

  @Override
  public void visit(StringLiteral expr) {
    push("'" + expr.getValue().replace("'", "''") + "'");
  }

  @Override
  public void visit(Int64Literal expr) {
    push(expr.getValue() + "L");
  }

  @Override
  public void visit(IntegralLiteral expr) {
    push(Integer.toString(expr.getValue()));
  }

  @Override
  public void visit(DoubleLiteral expr) {
    push(Double.toString(expr.getValue()));
  }

  @Override
  public void visit(SingleLiteral expr) {
    push(expr.getValue() + "f");
  }

  @Override
  public void visit(DecimalLiteral expr) {
    push(expr.getValue() + "M");
  }

  @Override
  public void visit(BinaryLiteral expr) {
    push("binary'" + Hex.encodeHexString(expr.getValue()) + "'");
  }

  @Override
  public void visit(DateTimeLiteral expr) {
    push("datetime'" + InternalUtil.formatDateTime(expr.getValue()) + "'");
  }

  @Override
  public void visit(DateTimeOffsetLiteral expr) {
    push("datetimeoffset'" + InternalUtil.formatDateTimeOffset(expr.getValue()) + "'");
  }

  @Override
  public void visit(TimeLiteral expr) {
    push("time'" + InternalUtil.toString(expr.getValue()) + "'");
  }

  @Override
  public void visit(ByteLiteral expr) {
    push(Hex.encodeHexString(new byte[] { expr.getValue() }));
  }

  // non-literals, not supported at the moment

  @Override
  public void beforeDescend() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void afterDescend() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void betweenDescend() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(String type) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(OrderByExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(AddExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(AndExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(CastExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ConcatMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(DivExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(EndsWithMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(EntitySimpleProperty expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(EqExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(GeExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(GtExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(IndexOfMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(IsofExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(LeExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(LengthMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(LtExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ModExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(MulExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(NeExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(NegateExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(NotExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(OrExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ParenExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(BoolParenExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ReplaceMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(StartsWithMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SubExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SubstringMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SubstringOfMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ToLowerMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ToUpperMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TrimMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(YearMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(MonthMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(DayMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(HourMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(MinuteMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SecondMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(RoundMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(FloorMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(CeilingMethodCallExpression expr) {
    throw new UnsupportedOperationException();
  }

}
