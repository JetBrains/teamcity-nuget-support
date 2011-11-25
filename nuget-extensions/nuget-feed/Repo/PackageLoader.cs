using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using JetBrains.Annotations;
using JetBrains.TeamCity.ServiceMessages.Read;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackageLoader
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly KeyValuePair<PropertyInfo, Action<TeamCityPackage, string>>[] Properties;

    public PackageLoader()
    {
      var myUnixEpoch = new DateTime(1970, 1, 1);
      var Types = new IParser[]
                    {
                      new Parser<string>(x => x),
                      new Parser<int>(x=>int.Parse(x)),
                      new Parser<long>(x=>long.Parse(x)),
                      new Parser<bool>(x=>bool.Parse(x)),
                      new Parser<DateTime>(x=>x[0] == 'j' ? (myUnixEpoch + new TimeSpan(long.Parse(x.Substring(1)) * TimeSpan.TicksPerMillisecond)) : DateTime.Parse(x)),
                      new Parser<DateTime?>(x => x == null ? (DateTime?) null : x[0] == 'j' ? myUnixEpoch + TimeSpan.FromMilliseconds(long.Parse(x.Substring(1))) : DateTime.Parse(x))
                    }
        .ToDictionary(x => x.Type);

      Properties = typeof(TeamCityPackage)
        .GetProperties()
        .Where(x => x.CanWrite && x.CanRead)
        .ToDictionary(x=>x, x=> Types[x.PropertyType].Compile(x)).ToArray();
    }

    public IEnumerable<string> GenerateRequiredParameterNames()
    {
      return from x in Properties select x.Key.Name;
    }

    public TeamCityPackage Load(IServiceMessage msg)
    {
      var pkg = new TeamCityPackage();
      for (int i = 0; i < Properties.Length; i++)
      {
        var ps = Properties[i];

        var value = msg.GetValue(ps.Key.Name);
        ps.Value(pkg, value);
      }

      return pkg;
    }



    private interface IParser
    {
      Type Type { get; }
      Action<TeamCityPackage, string> Compile(PropertyInfo property);
    }

    private class Parser<T> : IParser
    {
      public Type Type { get { return typeof (T); } }
      private readonly Expression<Func<string, T>> myParse;

      public Parser(Expression<Func<string, T>> parse)
      {
        myParse = parse;
      }

      public Action<TeamCityPackage, string> Compile(PropertyInfo property)
      {        
        if (typeof(T) != property.PropertyType)
          throw new ArgumentException("Type parameter must by == property type");

        var data = Expression.Parameter(typeof(string), "data");
        var package = Expression.Parameter(typeof(TeamCityPackage), "package");

        bool thowIfNull =  property.PropertyType.IsValueType || !property.IsDefined(typeof(CanBeNullAttribute), true) || property.IsDefined(typeof(NotNullAttribute), true);

        var expressions = new List<Expression>();
        if (thowIfNull)
        {
          expressions.Add(
            Expression.IfThen(
              Expression.ReferenceEqual(data, Expression.Constant(null, typeof (string))),
              Expression.Throw(
                Expression.New(typeof (RemoteException).GetConstructor(new[] {typeof (string)}),
                               Expression.Constant("Failed to find value for: " + property, typeof (string))))
              )
            );
        }

        expressions.Add(Expression.Assign(
            Expression.Property(package, property),
            Expression.Invoke(myParse, data)
            ));

        Expression<Action<TeamCityPackage, string>> expression = Expression.Lambda<Action<TeamCityPackage, string>>(
          Expression.Block(expressions),
          package, data);

        return expression.Compile();
      }

    }
  }
}