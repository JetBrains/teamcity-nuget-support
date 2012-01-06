/****
****
**** THIS CODE IS GENERATED BY jetbrains.buildServer.nuget.tests.server.entity.EntityGenerator$KeyBeanGenerator
**** DO NOT CHANGE!
*****/
package jetbrains.buildServer.nuget.server.feed.server.entity;

import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;

import java.util.Map;

public class PackageKey implements OEntityId { 
  protected final Map<String, String> myFields;

  public PackageKey(@NotNull final Map<String, String> data) {
    myFields = data;
  }

  public String getEntitySetName() {
    return "Packages";
  }

  public OEntityKey getEntityKey() {
    return OEntityKey.create("Id", getId(), "Version", getVersion());
  }

  public java.lang.String getId() { 
    final String v = myFields.get("Id");
    return v;
  }


  public java.lang.String getVersion() { 
    final String v = myFields.get("Version");
    return v;
  }


 public boolean isValid() { 
    if (!myFields.containsKey("Id")) return false;
    if (!myFields.containsKey("Version")) return false;
    return true;
  }
}

