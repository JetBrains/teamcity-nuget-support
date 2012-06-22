/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.mocks;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 22.06.12 14:21
*/
public class ListMatcher<T> extends BaseMatcher<Collection<T>> {
  private final Collection<Matcher<T>> myMatchers;

  public ListMatcher(@NotNull Collection<Matcher<T>> matchers) {
    myMatchers = matchers;
  }

  public boolean matches(Object o) {
    final Iterator th = ((Collection) o).iterator();
    final Iterator<Matcher<T>> my = myMatchers.iterator();

    while(th.hasNext() && my.hasNext()) {
      if (!my.next().matches(th.next())) return false;
    }

    return !th.hasNext() && !my.hasNext();
  }

  public void describeTo(Description description) {
    description.appendText("[");
    for (Matcher<T> matcher : myMatchers) {
      description.appendText(", ");
      matcher.describeTo(description);
    }
    description.appendText("]");
  }
}
