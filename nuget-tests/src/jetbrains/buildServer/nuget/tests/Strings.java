

package jetbrains.buildServer.nuget.tests;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:17
 */
public class Strings {
  private static String createExoticString() {
    try {
      StringBuilder sb = new StringBuilder();
      for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; i++) {
        try {
          sb.append(Character.valueOf(i));
        } catch (Throwable t) {
          // NOP
        }
      }
      return sb.toString();
    } catch (Throwable t) {
      return "failed to create exitic string. !@#$%^&*()}{POITREWQASDFGHJKL:\"|?><MNBVCXZ`1234567890-\\/\';][/*-\t\n\r\0\1";
    }
  }

  public static final String EXOTIC = createExoticString();
}
