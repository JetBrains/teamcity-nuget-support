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

package jetbrains.buildServer.nuget.tests.feed;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 18.07.11 2:21
*/
public class SetContentXmlPatchAction extends XmlPatchAction {
  private final String myRegex;
  private final String myReplace;

  SetContentXmlPatchAction(String XPath, String regex, String replace) {
    super(XPath);
    myRegex = regex;
    myReplace = replace;
  }

  @Override
  protected void action(@NotNull Element element) {
    String text = element.getText().trim();
    if (text.matches(myRegex)) {
      element.setText(myReplace);
    }
  }
}
