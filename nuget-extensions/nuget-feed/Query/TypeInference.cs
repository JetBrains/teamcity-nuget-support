using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.TeamCity.NuGet.Feed.Repo;

namespace JetBrains.TeamCity.NuGet.Feed.Query
{
  public static class TypeInference
  {
    public static Type FindIEnumerable(Type type)
    {
      //TODO: does not work for IOrderedQueriable'1
      if (type.IsArray)
        return typeof(IEnumerable<>).MakeGenericType(type.GetElementType());

      if (typeof(IEnumerable<TeamCityPackage>).IsAssignableFrom(type))
      {
        return typeof (TeamCityPackage);
      }

      var enu = type.GetInterfaces().Where(x => typeof (IEnumerable<>).IsAssignableFrom(type)).FirstOrDefault();
      if (enu != null)
        return enu.GetGenericArguments()[0];

      return null;
    }
  }
}