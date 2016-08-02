/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.QueryInfo;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public interface NuGetFeedFunction {
  @NotNull String getName();
  @NotNull EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType);
  @Nullable Iterable<Object> call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo);
}
