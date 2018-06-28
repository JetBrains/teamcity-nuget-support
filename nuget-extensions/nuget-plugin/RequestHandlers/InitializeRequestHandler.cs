using System.Diagnostics;
using System.Threading.Tasks;
using NuGet.Protocol.Plugins;

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
    /// <param name="logger">A <see cref="TraceSource"/> to use for logging.</param>
    public InitializeRequestHandler(TraceSource logger)
      : base(logger)
    {
    }

    public override Task<InitializeResponse> HandleRequestAsync(InitializeRequest request)
    {
      return Task.FromResult(new InitializeResponse(MessageResponseCode.Success));
    }
  }
}
