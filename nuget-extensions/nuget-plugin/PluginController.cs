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
      if (level < Logging)
      {
        return false;
      }
      
      if (Connection != null)
      {
        await Connection.SendRequestAndReceiveResponseAsync<LogRequest, LogResponse>(
          MessageMethod.Log, 
          new LogRequest(level, message), 
          CancellationToken.None);
        
        return true;
      }
      return false;
    }
  }
}
