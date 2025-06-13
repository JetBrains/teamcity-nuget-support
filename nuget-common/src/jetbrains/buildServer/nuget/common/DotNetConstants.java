

package jetbrains.buildServer.nuget.common;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:58
 */
public interface DotNetConstants {
  //NOTE: This is an implicit dependency to .NET runners agent plugin.
  //NOTE: For now there is no chance to share classes between plugins.
  public static final String MONO_PATH = "Mono_Path";
  public static final String DOTNET4VERSION_PATTERN = "DotNetFramework([4-9].*|\\d{2,}.*)_x86";
  public static final String DOTNET4_5VERSION_PATTERN = "DotNetFramework(4\\.[5-9]{1}.*|[5-9]{1}.*|\\d{2,}.*)_x86";
}
