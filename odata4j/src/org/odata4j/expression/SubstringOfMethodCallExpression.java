package org.odata4j.expression;

public interface SubstringOfMethodCallExpression extends BoolMethodExpression {

  CommonExpression getValue();

  CommonExpression getTarget(); // optional
}
