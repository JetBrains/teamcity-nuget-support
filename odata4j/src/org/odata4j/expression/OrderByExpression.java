package org.odata4j.expression;

public interface OrderByExpression extends CommonExpression {

  CommonExpression getExpression();

  boolean isAscending();
}
