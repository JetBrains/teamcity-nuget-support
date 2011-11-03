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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 15:14
 */
public class ServerXmlPatcher {
  public static void patchServerConfig(@NotNull final File xml, final int port) {
    final int shutdownPort = NetworkUtil.getFreePort(port+2);
    final int redirectPort = NetworkUtil.getFreePort(shutdownPort+2);


    FileUtil.processXmlFile(xml, new FileUtil.Processor() {

      private void setAttribute(@NotNull final Element root, @NotNull final String xpath, @NotNull Object value) {
        try {
          final Attribute shutdownAttribute = (Attribute) XPath.newInstance(xpath).selectSingleNode(root);
          if (shutdownAttribute != null) {
            shutdownAttribute.setValue(value.toString());
          }
        } catch (final JDOMException e) {
          throw new RuntimeException("Failed to patch config: " + xml + ". " + e.getMessage(), e);
        }

      }

      public void process(@NotNull Element root) {
        setAttribute(root, "/Server/@port", shutdownPort);
        setAttribute(root, "/Server/Service/Connector/@redirectPort", redirectPort);

      }
    });
  }
}
