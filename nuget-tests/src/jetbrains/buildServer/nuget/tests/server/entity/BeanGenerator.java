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

package jetbrains.buildServer.nuget.tests.server.entity;

import jetbrains.buildServer.nuget.feed.server.index.impl.ODataDataFormat;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmSimpleType;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 07.01.12 9:54
*/
public class BeanGenerator extends MethodsGenerator {
  public BeanGenerator(String name, Collection<MetadataBeanProperty> properties) {
    super(name, properties);
  }

  @Override
  protected void generateBeforeContent(@NotNull PrintWriter wr) {
  }

  @Override
  protected String getTypeKind() {
    return "abstract class";
  }

  @Override
  protected String getExtendsString() {
    String ext = getExtends();
    if (!StringUtil.isEmptyOrSpaces(ext)) {
      ext = " extends " + ext;
    }

    final Collection<String> impl = getImplements();
    if (!impl.isEmpty()) {
      ext += " implements " + StringUtil.join(", ", impl);
    }

    return ext;
  }

  @NotNull
  @Override
  protected String generatePropertyModifier(@NotNull MetadataBeanProperty p) {
    return "public final ";
  }

  protected void generatePropertyBody(@NotNull PrintWriter wr, @NotNull MetadataBeanProperty p) {
    final String name = p.getName();
    wr.println("{ ");
    wr.println("    final String v = getValue(\"" + name + "\");");
    if (!p.isNullable()) {
      wr.println("    if (v == null) { ");
      if (p.getType() == EdmSimpleType.STRING) {
        wr.println("      return \"\";");
      } else if (p.getType() == EdmSimpleType.BOOLEAN) {
        wr.println("      return false;");
      } else if (p.getType() == EdmSimpleType.INT32) {
        wr.println("      return 0;");
      } else if (p.getType() == EdmSimpleType.INT64) {
        wr.println("      return 0L;");
      } else if (p.getType() == EdmSimpleType.DATETIME) {
        wr.println("      return new org.joda.time.LocalDateTime();");
      } else {
        wr.println("    !!!Unsupported type!!!");
      }
      wr.println("    }");
    }
    if (p.getType() == EdmSimpleType.STRING) {
      wr.println("    return v;");
    } else if (p.getType() == EdmSimpleType.BOOLEAN){
      wr.println("    return Boolean.valueOf(v);");
    } else if (p.getType() == EdmSimpleType.INT32){
      wr.println("    return Integer.parseInt(v);");
    } else if (p.getType() == EdmSimpleType.INT64){
      wr.println("    return Long.parseLong(v);");
    } else if (p.getType() == EdmSimpleType.DATETIME){
      wr.println("    return " + ODataDataFormat.class.getName() + ".parseDate(v);");
    } else {
      wr.println("    UnsupportedTypeError");
    }
    wr.println("  }");
    wr.println();
  }

  protected void generateAfterContent(@NotNull final PrintWriter wr) {
    wr.println("  @Nullable");
    wr.println("  protected abstract String getValue(@NotNull final String key);");
    wr.println();
  }

  @NotNull
  protected String getExtends() {
    return "";
  }

  @NotNull
  protected Collection<String> getImplements() {
    return Collections.emptyList();
  }
}
