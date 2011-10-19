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

package jetbrains.buildServer.nuget.server.feed;

import org.odata4j.edm.*;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 3:08
 */
public class PackagesEntrySet {
  public static EdmEntitySet ENTRY_SET = new EdmEntitySet("Packages", new EdmEntityType(
          "banespace",
          "alias",
          "name",
          true,
          Arrays.asList("Id", "Version"),
          Arrays.asList(
                  new EdmProperty("Title", EdmType.STRING, true)
          ),
          Collections.<EdmNavigationProperty>emptyList()
  ));
}
