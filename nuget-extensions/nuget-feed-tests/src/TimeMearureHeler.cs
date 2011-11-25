using System;
using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  public static class TimeMearureHeler
  {
    public static void MeasureTime(TimeSpan time, int repeat, Action action)
    {
      var result = new List<TimeSpan>();
      while (repeat-- > 0)
      {
        var start = DateTime.Now;
        action();
        var span = DateTime.Now - start;

        Console.Out.WriteLine("Action finished in: {0}", span.TotalMilliseconds);

        if (span < time) return;
        result.Add(span);
      }

      Assert.Fail("Action is expected to complete in {0}, but was [{1}]", time.TotalMilliseconds, String.Join(", ", result.Select(x => x.TotalMilliseconds.ToString()).ToArray()));
    }

    public static void ExpectTime(this Action action, TimeSpan expectedTime, int maxRepeat)
    {
      MeasureTime(expectedTime, maxRepeat, action);
    }
    
  }
}