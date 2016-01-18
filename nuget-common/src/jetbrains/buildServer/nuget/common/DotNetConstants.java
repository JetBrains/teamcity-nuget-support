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

  public final static String v4_5 = "4.5";
  public final static String v4_5_1 = "4.5.1";
  public final static String v4_5_2 = "4.5.2";
  public final static String v4_6 = "4.6";
  public final static String v4_6_1 = "4.6.1";
}
