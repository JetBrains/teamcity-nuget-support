

package jetbrains.buildServer.nuget.feed.server.olingo.processor;

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.uri.expression.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Evaluates OData query expressions.
 */
public class ExpressionEvaluator {

  private final static Map<String, Comparator> COMPARATORS;
  private final BeanPropertyAccess valueAccess;

  public ExpressionEvaluator(BeanPropertyAccess valueAccess) {
    this.valueAccess = valueAccess;
  }

  public <T> String evaluateExpression(final T data, final CommonExpression expression) throws ODataException {
    switch (expression.getKind()) {
      case UNARY:
        final UnaryExpression unaryExpression = (UnaryExpression) expression;
        final String operand = evaluateExpression(data, unaryExpression.getOperand());

        switch (unaryExpression.getOperator()) {
          case NOT:
            return Boolean.toString(!Boolean.parseBoolean(operand));
          case MINUS:
            return operand.startsWith("-") ? operand.substring(1) : "-" + operand;
          default:
            throw new ODataNotImplementedException();
        }

      case BINARY:
        final BinaryExpression binaryExpression = (BinaryExpression) expression;
        CommonExpression leftOperand = binaryExpression.getLeftOperand();
        final String left = evaluateExpression(data, leftOperand);
        final String right = evaluateExpression(data, binaryExpression.getRightOperand());

        switch (binaryExpression.getOperator()) {
          case ADD:
            if (binaryExpression.getEdmType() == EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance()) {
              return Double.toString(Double.valueOf(left) + Double.valueOf(right));
            } else {
              return Long.toString(Long.valueOf(left) + Long.valueOf(right));
            }
          case SUB:
            if (binaryExpression.getEdmType() == EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance()) {
              return Double.toString(Double.valueOf(left) - Double.valueOf(right));
            } else {
              return Long.toString(Long.valueOf(left) - Long.valueOf(right));
            }
          case MUL:
            if (binaryExpression.getEdmType() == EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance()) {
              return Double.toString(Double.valueOf(left) * Double.valueOf(right));
            } else {
              return Long.toString(Long.valueOf(left) * Long.valueOf(right));
            }
          case DIV:
            final String number = Double.toString(Double.valueOf(left) / Double.valueOf(right));
            return number.endsWith(".0") ? number.replace(".0", "") : number;
          case MODULO:
            if (binaryExpression.getEdmType() == EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance()
              || binaryExpression.getEdmType() == EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance()) {
              return Double.toString(Double.valueOf(left) % Double.valueOf(right));
            } else {
              return Long.toString(Long.valueOf(left) % Long.valueOf(right));
            }
          case AND:
            return Boolean.toString(left.equals("true") && right.equals("true"));
          case OR:
            return Boolean.toString(left.equals("true") || right.equals("true"));
          case EQ:
            return Boolean.toString(compare(leftOperand, left, right) == 0);
          case NE:
            return Boolean.toString(compare(leftOperand, left, right) != 0);
          case LT:
            return Boolean.toString(compare(leftOperand, left, right) < 0);
          case LE:
            return Boolean.toString(compare(leftOperand, left, right) <= 0);
          case GT:
            return Boolean.toString(compare(leftOperand, left, right) > 0);
          case GE:
            return Boolean.toString(compare(leftOperand, left, right) >= 0);
          case PROPERTY_ACCESS:
            throw new ODataNotImplementedException();
          default:
            throw new ODataNotImplementedException();
        }

      case PROPERTY:
        final EdmProperty property = (EdmProperty) ((PropertyExpression) expression).getEdmProperty();
        final EdmSimpleType propertyType = (EdmSimpleType) property.getType();
        return propertyType.valueToString(valueAccess.getPropertyValue(data, property),
          EdmLiteralKind.DEFAULT, property.getFacets());

      case MEMBER:
        final MemberExpression memberExpression = (MemberExpression) expression;
        final PropertyExpression propertyExpression = (PropertyExpression) memberExpression.getProperty();
        final EdmProperty memberProperty = (EdmProperty) propertyExpression.getEdmProperty();
        final EdmSimpleType memberType = (EdmSimpleType) memberExpression.getEdmType();
        final List<EdmProperty> propertyPath = new ArrayList<>();
        CommonExpression currentExpression = memberExpression;

        while (currentExpression != null) {
          final PropertyExpression currentPropertyExpression =
            (PropertyExpression) (currentExpression.getKind() == ExpressionKind.MEMBER ?
              ((MemberExpression) currentExpression).getProperty() : currentExpression);
          final EdmTyped currentProperty = currentPropertyExpression.getEdmProperty();
          final EdmTypeKind kind = currentProperty.getType().getKind();
          if (kind == EdmTypeKind.SIMPLE || kind == EdmTypeKind.COMPLEX) {
            propertyPath.add(0, (EdmProperty) currentProperty);
          } else {
            throw new ODataNotImplementedException();
          }

          currentExpression = currentExpression.getKind() == ExpressionKind.MEMBER
            ? ((MemberExpression) currentExpression).getPath()
            : null;
        }

        final Object memberValue = valueAccess.getPropertyValue(data, propertyPath);
        return memberType.valueToString(memberValue, EdmLiteralKind.DEFAULT, memberProperty.getFacets());

      case LITERAL:
        final LiteralExpression literal = (LiteralExpression) expression;
        final EdmSimpleType literalType = (EdmSimpleType) literal.getEdmType();
        final Object literalValue = literalType.valueOfString(literal.getUriLiteral(),
          EdmLiteralKind.URI, null, literalType.getDefaultType());
        return literalType.valueToString(literalValue, EdmLiteralKind.DEFAULT, null);

      case METHOD:
        final MethodExpression methodExpression = (MethodExpression) expression;
        final String first = evaluateExpression(data, methodExpression.getParameters().get(0));
        final String second = methodExpression.getParameterCount() > 1 ?
          evaluateExpression(data, methodExpression.getParameters().get(1)) : "";
        final String third = methodExpression.getParameterCount() > 2 ?
          evaluateExpression(data, methodExpression.getParameters().get(2)) : "";

        switch (methodExpression.getMethod()) {
          case ENDSWITH:
            return Boolean.toString(first != null && first.endsWith(second));
          case INDEXOF:
            return Integer.toString(first == null ? -1 : first.indexOf(second));
          case STARTSWITH:
            return Boolean.toString(first != null && first.startsWith(second));
          case TOLOWER:
            return first == null ? null : first.toLowerCase(Locale.ROOT);
          case TOUPPER:
            return first == null ? null : first.toUpperCase(Locale.ROOT);
          case TRIM:
            return first == null ? null : first.trim();
          case SUBSTRING:
            final int offset = second == null || second.length() == 0 ? 0 : Integer.parseInt(second);
            final int length = third == null || third.length() == 0 ? 0 : Integer.parseInt(third);
            return first.substring(offset, offset + length);
          case SUBSTRINGOF:
            return Boolean.toString(second != null && second.contains(first));
          case CONCAT:
            return first + second;
          case LENGTH:
            return Integer.toString(first.length());
          case YEAR:
            return String.valueOf(Integer.parseInt(first.substring(0, 4)));
          case MONTH:
            return String.valueOf(Integer.parseInt(first.substring(5, 7)));
          case DAY:
            return String.valueOf(Integer.parseInt(first.substring(8, 10)));
          case HOUR:
            return String.valueOf(Integer.parseInt(first.substring(11, 13)));
          case MINUTE:
            return String.valueOf(Integer.parseInt(first.substring(14, 16)));
          case SECOND:
            return String.valueOf(Integer.parseInt(first.substring(17, 19)));
          case ROUND:
            return Long.toString(Math.round(Double.valueOf(first)));
          case FLOOR:
            return Long.toString(Math.round(Math.floor(Double.valueOf(first))));
          case CEILING:
            return Long.toString(Math.round(Math.ceil(Double.valueOf(first))));
          default:
            throw new ODataNotImplementedException();
        }

      default:
        throw new ODataNotImplementedException();
    }
  }

  private static int compare(@NotNull final CommonExpression expression,
                             @Nullable final String o1,
                             @Nullable final String o2) {
    if (o1 == null && o2 == null) return 0;
    if (o1 == null) return -1;
    if (o2 == null) return 1;

    //noinspection unchecked
    return COMPARATORS.get(getType(expression)).compare(o1, o2);
  }

  private static String getType(final CommonExpression expression) {
    final EdmSimpleType edmType = (EdmSimpleType) expression.getEdmType();
    final String literal = expression.getUriLiteral();
    if (VERSION.equals(literal) || NORMALIZED_VERSION.equals(literal)) {
      return VERSION;
    } else {
      try {
        return edmType.getName();
      } catch (EdmException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static {
    COMPARATORS = new HashMap<>();
    COMPARATORS.put("Binary", Comparator.comparing(o -> Boolean.valueOf((String) o)));
    COMPARATORS.put("Boolean", Comparator.comparing(o -> Boolean.valueOf((String) o)));
    COMPARATORS.put("Byte", Comparator.comparing(o -> Byte.valueOf((String) o)));
    COMPARATORS.put("DateTime", Comparator.comparing(o -> LocalDateTime.parse((String) o)));
    COMPARATORS.put("DateTimeOffset", Comparator.comparing(o -> Duration.parse((String) o)));
    COMPARATORS.put("Decimal", Comparator.comparing(o -> Double.valueOf((String) o)));
    COMPARATORS.put("Double", Comparator.comparing(o -> Double.valueOf((String) o)));
    COMPARATORS.put("Guid", Comparator.comparing(o -> UUID.fromString((String) o)));
    COMPARATORS.put("Int16", Comparator.comparing(o -> Short.valueOf((String) o)));
    COMPARATORS.put("Int32", Comparator.comparing(o -> Integer.valueOf((String) o)));
    COMPARATORS.put("Int64", Comparator.comparing(o -> Long.valueOf((String) o)));
    COMPARATORS.put("Null", (o1, o2) -> o1.equals(o2) ? 0 : -1);
    COMPARATORS.put("SByte", Comparator.comparing(o -> Byte.valueOf((String) o)));
    COMPARATORS.put("Single", Comparator.comparing(o -> Double.valueOf((String) o)));
    COMPARATORS.put("String", Comparator.comparing(o -> ((String) o)));
    COMPARATORS.put("Time", Comparator.comparing(o -> LocalDateTime.parse((String) o)));
    COMPARATORS.put(VERSION, (o1, o2) -> {
      SemanticVersion v1 = SemanticVersion.valueOf((String) o1);
      SemanticVersion v2 = SemanticVersion.valueOf((String) o2);
      if (v1 != null && v2 != null) {
        return v1.compareTo(v2);
      } else if (v1 == null) {
        return 1;
      } else {
        return -1;
      }
    });
  }
}
