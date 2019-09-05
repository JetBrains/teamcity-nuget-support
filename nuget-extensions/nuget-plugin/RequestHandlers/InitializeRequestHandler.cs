using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles an <see cref="InitializeRequest"/>.
  /// </summary>
  internal class InitializeRequestHandler : RequestHandlerBase<InitializeRequest, InitializeResponse>
  {
    /// <summary>
    /// Initializes a new instance of the <see cref="InitializeRequestHandler"/> class.
    /// </summary>
    /// <param name="logger">A <see cref="Logging.ILogger"/> to use for logging.</param>
    public InitializeRequestHandler(ILogger logger)
      : base(logger)
    {
    }

    public override Task<InitializeResponse> HandleRequestAsync(InitializeRequest request)
    {
      Logger.Log(LogLevel.Verbose, $"Request timeout: {request.RequestTimeout}");
      return Task.FromResult(new InitializeResponse(MessageResponseCode.Success));
    }
  }
}
