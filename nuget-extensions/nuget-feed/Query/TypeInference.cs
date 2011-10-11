using System;
using System.Collections.Generic;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public static class TypeInference
  {
    public static Type FindIEnumerable(Type type)
    {
      if (type.IsArray)
        return typeof(IEnumerable<>).MakeGenericType(type.GetElementType());

      var enu = type.GetInterfaces().Where(x => typeof (IEnumerable<>).IsAssignableFrom(type)).FirstOrDefault();
      if (enu != null)
        return enu.GetGenericArguments()[0];

      return null;
    }
  }
}