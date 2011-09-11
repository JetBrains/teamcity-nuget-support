package org.odata4j.expression;

import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

public class PrintExpressionVisitor implements ExpressionVisitor {

  private final StringBuilder sb = new StringBuilder();

  @Override
  public String toString() {
    return sb.toString();
  }

  private void append(String value) {
    sb.append(value);
  }

  private void append(String format, Object... args) {
    sb.append(String.format(format, args));
  }

  @Override
  public void visit(String type) {
    append(type);
  }

  @Override
  public void afterDescend() {
    append(")");

  }

  @Override
  public void beforeDescend() {
    append("(");

  }

  @Override
  public void betweenDescend() {
    append(",");

  }

  @Override
  public void visit(AddExpression expr) {
    append("add");
  }

  @Override
  public void visit(AndExpression expr) {
    append("and");
  }

  @Override
  public void visit(BooleanLiteral expr) {
    append("boolean(%s)", expr.getValue());
  }

  @Override
  public void visit(CastExpression expr) {
    append("cast");
  }

  @Override
  public void visit(ConcatMethodCallExpression expr) {
    append("concat");
  }

  @Override
  public void visit(DateTimeLiteral expr) {
    append("datetime(%s)", InternalUtil.formatDateTime(expr.getValue()));
  }

  @Override
  public void visit(DateTimeOffsetLiteral expr) {
    append("datetime(%s)", InternalUtil.formatDateTimeOffset(expr.getValue()));
  }

  @Override
  public void visit(DecimalLiteral expr) {
    append("decimal(%s)", expr.getValue());
  }

  @Override
  public void visit(DivExpression expr) {
    append("div");
  }

  @Override
  public void visit(EndsWithMethodCallExpression expr) {
    append("endswith");
  }

  @Override
  public void visit(EntitySimpleProperty expr) {
    append("simpleProperty(%s)", expr.getPropertyName());
  }

  @Override
  public void visit(EqExpression expr) {
    append("eq");
  }

  @Override
  public void visit(GeExpression expr) {
    append("ge");
  }

  @Override
  public void visit(GtExpression expr) {
    append("gt");
  }

  @Override
  public void visit(GuidLiteral expr) {
    append("guid(%s)", expr.getValue());
  }

  @Override
  public void visit(IndexOfMethodCallExpression expr) {
    append("indexof");
  }

  @Override
  public void visit(IntegralLiteral expr) {
    append("integral(%s)", expr.getValue());
  }

  @Override
  public void visit(IsofExpression expr) {
    append("isof");
  }

  @Override
  public void visit(LeExpression expr) {
    append("le");
  }

  @Override
  public void visit(LengthMethodCallExpression expr) {
    append("length");
  }

  @Override
  public void visit(LtExpression expr) {
    append("lt");
  }

  @Override
  public void visit(ModExpression expr) {
    append("mod");
  }

  @Override
  public void visit(MulExpression expr) {
    append("mul");
  }

  @Override
  public void visit(NeExpression expr) {
    append("ne");
  }

  @Override
  public void visit(NegateExpression expr) {
    append("negate");
  }

  @Override
  public void visit(NotExpression expr) {
    append("not");
  }

  @Override
  public void visit(NullLiteral expr) {
    append("null");
  }

  @Override
  public void visit(OrExpression expr) {
    append("or");
  }

  @Override
  public void visit(ParenExpression expr) {
    append("paren");
  }

  @Override
  public void visit(BoolParenExpression expr) {
    append("boolParen");
  }

  @Override
  public void visit(ReplaceMethodCallExpression expr) {
    append("replace");
  }

  @Override
  public void visit(StartsWithMethodCallExpression expr) {
    append("startswith");
  }

  @Override
  public void visit(StringLiteral expr) {
    append("string(%s)", expr.getValue());
  }

  @Override
  public void visit(SubExpression expr) {
    append("sub");
  }

  @Override
  public void visit(SubstringMethodCallExpression expr) {
    append("substring");
  }

  @Override
  public void visit(SubstringOfMethodCallExpression expr) {
    append("substringof");
  }

  @Override
  public void visit(TimeLiteral expr) {
    append("time(%s)", expr.getValue().toString(ExpressionParser.TIME_FORMATTER));
  }

  @Override
  public void visit(ToLowerMethodCallExpression expr) {
    append("tolower");
  }

  @Override
  public void visit(ToUpperMethodCallExpression expr) {
    append("toupper");
  }

  @Override
  public void visit(TrimMethodCallExpression expr) {
    append("trim");
  }

  @Override
  public void visit(YearMethodCallExpression expr) {
    append("year");
  }

  @Override
  public void visit(MonthMethodCallExpression expr) {
    append("month");
  }

  @Override
  public void visit(DayMethodCallExpression expr) {
    append("day");
  }

  @Override
  public void visit(HourMethodCallExpression expr) {
    append("hour");
  }

  @Override
  public void visit(MinuteMethodCallExpression expr) {
    append("minute");
  }

  @Override
  public void visit(SecondMethodCallExpression expr) {
    append("second");
  }

  @Override
  public void visit(RoundMethodCallExpression expr) {
    append("round");
  }

  @Override
  public void visit(FloorMethodCallExpression expr) {
    append("floor");
  }

  @Override
  public void visit(CeilingMethodCallExpression expr) {
    append("ceiling");
  }

  @Override
  public void visit(OrderByExpression expr) {
    append("orderBy");
  }

  @Override
  public void visit(Int64Literal expr) {
    append("int64(%s)", expr.getValue());
  }

  @Override
  public void visit(SingleLiteral expr) {
    append("single(%s)", expr.getValue());
  }

  @Override
  public void visit(DoubleLiteral expr) {
    append("double(%s)", expr.getValue());
  }

  @Override
  public void visit(BinaryLiteral expr) {
    append("binary(%s)", Hex.encodeHexString(expr.getValue()));
  }

  @Override
  public void visit(ByteLiteral expr) {
    append("byte(%s)", expr.getValue());
  }

}
