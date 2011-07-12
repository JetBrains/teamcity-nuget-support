/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common.dependencies;

import com.sun.istack.internal.NotNull;

/**
 * Represents virual file to allow using it both
 * on server (where no checkout directory) and on agent
 *
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 21:25
 */
public interface RelativePath {
  /**
   * @return parent file of this file
   */
  @NotNull
  RelativePath getParent();

  /**
   * @param relative relative path
   * @return constructed relative file
   */
  @NotNull
  RelativePath createChild(@NotNull String relative);
}
