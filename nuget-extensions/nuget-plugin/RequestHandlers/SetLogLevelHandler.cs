using System.Threading.Tasks;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="SetLogLevelRequest"/> and replies with credentials.
  /// </summary>
  internal class SetLogLevelHandler : RequestHandlerBase<SetLogLevelRequest, SetLogLevelResponse>
  {
    public SetLogLevelHandler(PluginController plugin) : base(plugin)
    {
    }

    public override Task<SetLogLevelResponse> HandleRequestAsync(SetLogLevelRequest request)
    {
      Plugin.Logging = request.LogLevel;
      return Task.FromResult(new SetLogLevelResponse(MessageResponseCode.Success));
    }
  }
}
