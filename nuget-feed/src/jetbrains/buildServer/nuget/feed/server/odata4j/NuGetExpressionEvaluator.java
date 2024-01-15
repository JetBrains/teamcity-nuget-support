

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import org.odata4j.expression.BinaryCommonExpression;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.producer.inmemory.InMemoryEvaluationImpl;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * NuGet expression evaluator.
 */
public class NuGetExpressionEvaluator extends InMemoryEvaluationImpl {

    @Override
    protected int compareTo(BinaryCommonExpression expression, InMemoryEvaluationImpl.ObjectPair pair) {
        CommonExpression lhs = expression.getLHS();
        if (lhs instanceof EntitySimpleProperty) {
            String name = ((EntitySimpleProperty) lhs).getPropertyName();
            if (VERSION.equals(name) || NORMALIZED_VERSION.equals(name)) {
                SemanticVersion v1 = SemanticVersion.valueOf((String) pair.lhs);
                SemanticVersion v2 = SemanticVersion.valueOf((String) pair.rhs);
                if (v1 != null && v2 != null) {
                    return v1.compareTo(v2);
                }
            }
        }

        return super.compareTo(expression, pair);
    }
}
