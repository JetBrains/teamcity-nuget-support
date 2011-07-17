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

package jetbrains.buildServer.nuget.server.feed;

import org.odata4j.core.*;
import org.odata4j.edm.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 18.07.11 1:58
*/
public class PackageEntry implements OEntity, AtomInfo {

  private OProperty<Object> myTitleProperty = new OProperty<Object>() {
      public EdmType getType() {
        return EdmType.STRING;
      }

      public String getName() {
        return "Title";
      }

      public Object getValue() {
        return "This is title";
      }
    };

  public String getTitle() {
    return "Atom title";
  }

  public String getCategoryTerm() {
    return "Atom term";
  }

  public EdmEntitySet getEntitySet() {
    return PackagesEntrySet.ENTRY_SET;
  }

  public OEntityKey getEntityKey() {
    return OEntityKey.create(
            new TreeMap<String, Object>(){{
              put("Id", "package.id");
              put("Version", "4.5.3.2");
            }}
    );
  }

  public List<OProperty<?>> getProperties() {
        return Arrays.<OProperty<?>>asList(myTitleProperty);
  }

  public OProperty<?> getProperty(String propName) {
    for (OProperty<?> oProperty : getProperties()) {
      if (oProperty.getName().equals(propName))
        return oProperty;
    }
    return null;
  }

  public <T> OProperty<T> getProperty(String propName, Class<T> propClass) {
    return (OProperty<T>) getProperty(propName);
  }

  public List<OLink> getLinks() {
    return Collections.emptyList();
  }

  public <T extends OLink> T getLink(String title, Class<T> linkClass) {
    return null;
  }
}
