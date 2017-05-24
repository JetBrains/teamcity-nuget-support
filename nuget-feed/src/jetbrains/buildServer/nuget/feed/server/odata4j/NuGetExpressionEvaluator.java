/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.server.version.SemanticVersion;
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
