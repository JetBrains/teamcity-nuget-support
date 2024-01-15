

package jetbrains.buildServer.nuget.tests.agent;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 08.07.11 1:15
*/
public class StartsWithMatcher extends BaseMatcher<String> {
  private final String myPrefix;

  public StartsWithMatcher(@NotNull final String prefix) {
    myPrefix = prefix;
  }

  public boolean matches(Object o) {
    return o instanceof String && ((String) o).startsWith(myPrefix);
  }

  public void describeTo(Description description) {
    description.appendText("String starts with ").appendValue(myPrefix);
  }
}
