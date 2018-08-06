using System.Threading;
using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  class PluginController
  {
    public IConnection Connection { get; set; }

    public LogLevel Logging { get; set; }

    public async Task<bool> LogMessageAsync(LogLevel level, string message)
    {
      if (level < Logging || Connection == null)
      {
        return false;
      }

//      Task.Run(async () =>
//               {
//                 await Connection.SendRequestAndReceiveResponseAsync<LogRequest, LogResponse>(
//                   MessageMethod.Log,
//                   new LogRequest(level, message),
//                   CancellationToken.None).ConfigureAwait(false);
//               });

      return true;
    }
  }
}
