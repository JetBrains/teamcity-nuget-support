

package jetbrains.buildServer.nuget.tests.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 13.01.12 13:26
 */
public class FeedParseResult {
  private final Set<String> myProperties;
  private final Map<String, String> myAtomProperties;


  public FeedParseResult(@NotNull final Collection<String> properties,
                         @NotNull final Map<String, String> atomProperties) {
    myAtomProperties = new TreeMap<String, String>(atomProperties);
    myProperties = new TreeSet<String>(properties);
  }

  @NotNull
  public Set<String> getPropertyNames() {
    return myProperties;
  }

  @NotNull
  public Map<String, String> getAtomProperties() {
    return myAtomProperties;
  }
}
