using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using JetBrains.Annotations;
using System.Linq;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class CommandLineHelper
  {
    private static readonly Regex CAN_NOT_QUOTE = new Regex(@"^[a-z\\/:0-9\._+=]*$", RegexOptions.IgnoreCase);
    private static readonly Regex SHOULD_QUOTE = new Regex("[|><\\s,;\"]+", RegexOptions.None);
    private static readonly Regex NEWLINE_QUOTE = new Regex("[\\r\\n\\t]+", RegexOptions.None);

    private static void SafeAppendParameter(StringBuilder sb, string text)
    {
      bool quote = SHOULD_QUOTE.IsMatch(text) || !CAN_NOT_QUOTE.IsMatch(text);
      if (quote)
        sb.Append('"');

      if (text.IndexOf('"') >= 0)
        text = text.Replace("\\\"", "\\\\\"").Replace("\"", "\\\"");

      sb.Append(text);

      if (quote)
      {
        if (text.EndsWith(@"\"))
          sb.Append('\\');

        sb.Append('"');
      }
    }

    [NotNull]
    public static string[] SplitCommandLine([NotNull] string s)
    {
      s = NEWLINE_QUOTE.Replace(s, " ");

      const char separator = ' ';
      var quoted = new ArrayList();
      var builder = new StringBuilder();
      bool inQuotes = false;
      for (int i = 0; i < s.Length; i++)
      {
        char c = s[i];
        if (c == separator && !inQuotes)
        {
          if (builder.Length > 0)
          {
            quoted.Add(builder.ToString());
            builder = new StringBuilder();
          }
          continue;
        }

        if (c == '"' && !(i > 0 && s[i - 1] == '\\'))
        {
          inQuotes = !inQuotes;
        }
        builder.Append(c);
      }

      if (builder.Length > 0)
      {
        quoted.Add(builder.ToString());
      }

      var result = new ArrayList();
      foreach (string q in quoted)
      {
        result.Add(UnquoteIfNeeded(q));
      }

      return (string[])result.ToArray(typeof(string));
    }

    private static string UnquoteIfNeeded(string s)
    {
      if (s != null && s.Length >= 2)
      {
        if (s[0] == '\"' && s[s.Length - 1] == '\"')
        {
          return UnquoteIfNeeded(s.Substring(1, s.Length - 2));
        }
      }
      return s;
    }

    public static string Join(IEnumerable<string> arguments)
    {
      return Join(0, arguments.ToArray());
    }

    public static string EscapeArgument(string arg)
    {
      return Join(new[]{arg});
    }

    public static string Join(int offset, params string[] arguments)
    {
      var sb = new StringBuilder();
      bool isFirst = true;
      for (int i = offset; i < arguments.Length; i++)
      {
        string arg = arguments[i];
        if (!isFirst)
          sb.Append(' ');
        else
          isFirst = false;

        SafeAppendParameter(sb, arg);
      }
      return sb.ToString();
    }
  }
}