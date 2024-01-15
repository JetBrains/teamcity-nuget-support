

package jetbrains.buildServer.nuget.common;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:58
 */
public interface DotNetConstants {
  //NOTE: This is an implicit dependency to .NET runners agent plugin.
  //NOTE: For now there is no chance to share classes between plugins.

  public static final String DOT_NET_FRAMEWORK = "DotNetFramework";
  public static final String DOT_NET_FRAMEWORK_4_x86 = "DotNetFramework4.0_x86";
  public static final String MONO_VERSION_PATTERN = "Mono(3\\.[2-9]{1}.*|3\\.\\d{2,}.*|[4-9]{1}.*|[\\d]{2,}.*)";
  public static final String DOTNET4VERSION_PATTERN = "DotNetFramework([4-9].*|\\d{2,}.*)_x86";
  public static final String DOTNET4_5VERSION_PATTERN = "DotNetFramework(4\\.[5-9]{1}.*|[5-9]{1}.*|\\d{2,}.*)_x86";

  public final static String v4_5 = "4.5";
  public final static String v4_5_1 = "4.5.1";
  public final static String v4_5_2 = "4.5.2";
  public final static String v4_6 = "4.6";
  public final static String v4_6_1 = "4.6.1";
}
