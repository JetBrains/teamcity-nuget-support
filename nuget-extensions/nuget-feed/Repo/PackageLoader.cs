using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using JetBrains.Annotations;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackageLoader
  {
    private readonly Dictionary<PropertyInfo, IParser> Properties;

    public PackageLoader()
    {
      var Types = new IParser[]
                    {
                      new Parser<string>(x => x),
                      new Parser<int>(int.Parse),
                      new Parser<long>(long.Parse),
                      new Parser<bool>(bool.Parse),
                      new Parser<DateTime>(DateTime.Parse),
                      new Parser<DateTime?>(x => x == null ? (DateTime?) null : DateTime.Parse(x))
                    }
        .ToDictionary(x => x.Type);

      Properties = typeof(TeamCityPackage)
        .GetProperties()
        .Where(x => x.CanWrite && x.CanRead)
        .ToDictionary(x=>x, x=>Types[x.PropertyType]);
    }

    public IEnumerable<string> GenerateRequiredParameterNames()
    {
      return from x in Properties select x.Key.Name;
    }

    public TeamCityPackage Load(IServiceMessage msg)
    {
      var pkg = new TeamCityPackage();
      foreach (var ps in Properties)
      {
        var value = msg.GetValue(ps.Key.Name);
        if (value == null && ps.Key.PropertyType.IsByRef)
        {
          if (!ps.Key.IsDefined(typeof(CanBeNullAttribute), true) || ps.Key.IsDefined(typeof(NotNullAttribute), true))
            throw new RemoteException("Failed to find value for: " + ps.Key);
        }

        if (value == null && ps.Key.PropertyType.IsValueType)
          throw new RemoteException("Failed to find value for: " + ps.Key);

        ps.Key.SetValue(pkg, ps.Value.Parse(value), null);
      }

      return pkg;
    }


    private interface IParser
    {
      Type Type { get; }
      Func<string, object> Parse { get; }
    }

    private class Parser<T> : IParser
    {
      public Type Type { get { return typeof (T); } }
      public Func<string, object> Parse { get; private set;}

      public Parser(Func<string, T> parse)
      {
        Parse = x => parse(x);
      }
    }
  }
}