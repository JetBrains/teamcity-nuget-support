using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Util
{
  public static class ServiceMessageFormatter
  {
    private static readonly Pair[] ESCAPINGS = {
                                                 new Pair('|', "||"), 
                                                 new Pair('\'', "|'"), 
                                                 new Pair('\n', "|n"), 
                                                 new Pair('\r', "|r"), 
                                                 new Pair(']', "|]")
                                               };

    private const string SERVICE_MESSAGE_HEADER = "##teamcity[";

    /// <summary>
    /// Performs TeamCity-format escaping of a string.
    /// </summary>
    /// <returns></returns>
    private static string Escape(string value)
    {
      return ESCAPINGS.Aggregate(value, (current, pair) => current.Replace(pair.What, pair.Replacement));
    }

    private struct Pair
    {
      public readonly string What;
      public readonly string Replacement;

      private Pair(string what, string replacement)
      {
        What = what;
        Replacement = replacement;
      }

      public Pair(char what, string replacement)
        : this(what.ToString(), replacement)
      {
      }
    }

    public static string FormatMessage(string sMessageName, string sValue)
    {
      if (string.IsNullOrEmpty(sMessageName))
        throw new ArgumentNullException("sMessageName");
      if (sValue == null)
        throw new ArgumentNullException("sValue");

      if (Escape(sMessageName) != sMessageName)
        throw new ArgumentException("The message name contains illegal characters.", "sMessageName");

      return string.Format("{2}{0} '{1}']", sMessageName, Escape(sValue), SERVICE_MESSAGE_HEADER);
    }

    public static string FormatMessage(string sMessageName, params ServiceMessageProperty[] properties)
    {
      if (string.IsNullOrEmpty(sMessageName))
        throw new ArgumentNullException("sMessageName");
      if (properties == null)
        throw new ArgumentNullException("properties");

      if (Escape(sMessageName) != sMessageName)
        throw new ArgumentException("The message name contains illegal characters.", "sMessageName");

      var sb = new StringBuilder();
      sb.AppendFormat("{1}{0}", sMessageName, SERVICE_MESSAGE_HEADER);
      foreach (var property in properties)
      {
        if (Escape(property.Key) != property.Key)
          throw new InvalidOperationException(string.Format("The property name “{0}” contains illegal characters.", property.Key));
        sb.AppendFormat(" {0}='{1}'", property.Key, Escape(property.Value));
      }
      sb.Append(']');

      return sb.ToString();
    }
  }
}