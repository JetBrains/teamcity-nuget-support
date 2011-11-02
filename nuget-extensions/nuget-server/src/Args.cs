using System;
using System.Collections.Generic;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Server
{
  public class Args
  {
    private readonly List<string> myArgs;

    public Args(IEnumerable<string> args)
    {
      myArgs = new List<string>(args);
    }

    public bool Contains(string key)
    {
      return myArgs.Any(arg => arg.Equals("/" + key, StringComparison.InvariantCultureIgnoreCase));
    }

    public string Get(string key, string def = null)
    {
      string lookup = "/" + key + ":";
      foreach (string arg in myArgs)
      {
        if (arg.StartsWith(lookup, StringComparison.InvariantCultureIgnoreCase))
        {
          return arg.Substring(lookup.Length);
        }
      }
      return def;
    }

    public int GetInt(string key, int def)
    {
      var text = Get(key, null);
      try
      {
        return int.Parse(text);
      } catch
      {
        return def;
      }
    }
  }
}