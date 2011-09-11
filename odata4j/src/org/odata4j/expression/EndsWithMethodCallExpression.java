package org.odata4j.expression;

public interface EndsWithMethodCallExpression extends BoolMethodExpression {

  CommonExpression getTarget();

  CommonExpression getValue();
}
