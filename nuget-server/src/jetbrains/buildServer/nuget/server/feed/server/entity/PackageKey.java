package jetbrains.buildServer.nuget.server.feed.server.entity;

import java.util.*;
import java.lang.*;

public class PackageKey { 
  private final Map<String, Object> myFields = new HashMap<String, Object>();


  public java.lang.String getId() { 
    return java.lang.String.class.cast(myFields.get("Id"));
  }

  public void setId(final java.lang.String v) { 
    myFields.put("Id", v);
  }


  public java.lang.String getVersion() { 
    return java.lang.String.class.cast(myFields.get("Version"));
  }

  public void setVersion(final java.lang.String v) { 
    myFields.put("Version", v);
  }


 public boolean isValid() { 
    if (!myFields.containsKey("Id")) return false;
    if (!myFields.containsKey("Version")) return false;
    return true;
  }
}

