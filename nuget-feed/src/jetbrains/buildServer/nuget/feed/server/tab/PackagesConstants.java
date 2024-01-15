

package jetbrains.buildServer.nuget.feed.server.tab;

import jetbrains.buildServer.serverSide.packages.RepositoryConstants;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 31.10.11 11:56
 */
public class PackagesConstants {

  public String getType() {
      return RepositoryConstants.REPOSITORY_TYPE_KEY;
  }

  public String getName() {
      return RepositoryConstants.REPOSITORY_NAME_KEY;
  }

  public String getDescription() {
      return RepositoryConstants.REPOSITORY_DESCRIPTION_KEY;
  }
}
