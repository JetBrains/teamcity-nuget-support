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
