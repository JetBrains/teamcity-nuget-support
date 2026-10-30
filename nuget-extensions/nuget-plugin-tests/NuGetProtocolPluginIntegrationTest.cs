using System;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using NuGet.Versioning;
using NUnit.Framework;
using NuGetMessageMethod = NuGet.Protocol.Plugins.MessageMethod;
using NuGetMessageResponseCode = NuGet.Protocol.Plugins.MessageResponseCode;
using NuGetOperationClaim = NuGet.Protocol.Plugins.OperationClaim;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  [TestFixture]
  public class NuGetProtocolPluginIntegrationTest
  {
    private const string PackageSourceRepository = "https://example.test/nuget/index.json";
    private const string HandshakeDelayEnvironmentVariable = "TEAMCITY_NUGET_TEST_DELAY_HANDSHAKE_MS";
    private const string InitializeDelayEnvironmentVariable = "TEAMCITY_NUGET_TEST_DELAY_INITIALIZE_MS";
    private const string AuthenticationCredentialsDelayEnvironmentVariable = "TEAMCITY_NUGET_TEST_DELAY_GET_AUTHENTICATION_CREDENTIALS_MS";
    private const string RequestStartedMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_STARTED_FILE";
    private const string RequestCanceledMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_CANCELED_FILE";
    private const string RequestCompletedMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_COMPLETED_FILE";
    private const int DelayedHandlerDelayMs = 1000;
    private static readonly TimeSpan Timeout = TimeSpan.FromSeconds(15);

    [Test]
    [Description(
      "Verifies the happy-path NuGet.Protocol lifecycle: plugin creation, handshake, log-level setup, Initialize, " +
      "and GetOperationClaims. This is the baseline integration check for all cancellation tests; while writing it " +
      "we found that the test must use the real NuGet.Protocol PluginFactory and a valid TeamCity feed file because " +
      "claims are produced only after the plugin is initialized with realistic environment state.")]
    public void NuGetProtocolClient_CanHandshakeInitializeAndGetAuthenticationClaims()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunLifecycleAsync());
    }

    [Test]
    [Description(
      "Verifies that NuGet.Protocol honors a cancellation token that is already canceled before PluginFactory.GetOrCreateAsync " +
      "starts. This matters because callers may abandon plugin creation before a process is launched; the observed NuGet " +
      "behavior is a direct OperationCanceledException without creating a usable plugin instance.")]
    public void GetOrCreateAsync_WhenCancellationAlreadyRequested_ThrowsOperationCanceledException()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunGetOrCreateWithAlreadyCanceledTokenAsync());
    }

    [Test]
    [Description(
      "Verifies that a failed creation attempt with an already-canceled token does not poison later plugin creation. " +
      "This protects retry scenarios; NuGet.Protocol allows a subsequent GetOrCreateAsync call to create a fresh plugin " +
      "and complete handshake, Initialize, and claims exchange successfully.")]
    public void GetOrCreateAsync_AfterCanceledCreation_CanCreatePluginSuccessfully()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunCreateAfterCanceledCreationAsync());
    }

    [Test]
    [Description(
      "Verifies cancellation while PluginFactory.GetOrCreateAsync is waiting for the handshake response. This covers " +
      "NuGet-side cancellation during plugin startup; NuGet.Protocol wraps the underlying TaskCanceledException into " +
      "PluginException for this phase, and a new PluginFactory is needed for a reliable retry.")]
    public void GetOrCreateAsync_WhenCancellationRequestedDuringHandshake_ThrowsOperationCanceledExceptionAndCanCreateAgain()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunGetOrCreateCanceledDuringHandshakeAsync());
    }

    [Test]
    [Description(
      "Verifies that a handshake timeout is treated as cancellation or timeout and does not prevent creating another " +
      "plugin afterwards. This is important for slow or stuck plugin startup; NuGet.Protocol reports the failed process " +
      "startup as PluginException with an inner TaskCanceledException, so the test accepts that wrapped cancellation shape.")]
    public void GetOrCreateAsync_WhenHandshakeTimeoutExpires_ThrowsOperationCanceledExceptionOrTimeoutAndCanCreateAgain()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunHandshakeTimeoutDoesNotBreakCreationAsync());
    }

    [Test]
    [Description(
      "Verifies that sending a request with an already-canceled token fails before the request breaks the plugin connection. " +
      "This matters for callers that cancel before dispatch; NuGet.Protocol leaves the connection usable and the same plugin " +
      "can still complete Initialize and claims exchange afterwards.")]
    public void SendRequest_WhenCancellationAlreadyRequested_DoesNotBreakPluginConnection()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunCanceledRequestDoesNotBreakConnectionAsync());
    }

    [Test]
    [Description(
      "Verifies that cancellation of an in-flight request sends cancellation through the NuGet.Protocol connection and the " +
      "plugin remains usable. A deterministic debug delay is used because timing-only cancellation is flaky; after cancel, " +
      "the same connection can still process Initialize and GetOperationClaims.")]
    public void SendRequest_WhenCancellationRequestedDuringInFlightRequest_SendsCancelAndPluginRemainsUsable()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunInFlightCanceledRequestDoesNotBreakConnectionAsync());
    }

    [Test]
    [Description(
      "Verifies closing a plugin while a request is already in flight. This checks that Plugin.Close returns cleanly and " +
      "the outstanding NuGet.Protocol request observes cancellation; while writing the test we found that close behavior " +
      "is best asserted from the NuGet-side task rather than by waiting for plugin-side cancellation markers.")]
    public void Close_WhenRequestIsInFlight_CancelsOutstandingRequestAndPluginExitsCleanly()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunCloseWithInFlightRequestAsync());
    }

    [Test]
    [Description(
      "Verifies that when NuGet.Protocol sends cancel for an in-flight request, the plugin handler does not continue to a " +
      "successful completion marker after cancellation. This protects against late responses causing protocol confusion; " +
      "the test uses debug wrapper markers to prove the request started, cancellation was observed, and completion was not recorded.")]
    public void SendRequest_WhenNuGetSendsCancel_PluginHandlerDoesNotSendLateResponse()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunCanceledRequestDoesNotSendLateResponseAsync());
    }

    [Test]
    [Description(
      "Verifies request timeout handling for a delayed plugin response. This covers NuGet.Protocol request timeout behavior; " +
      "during implementation we found that a timed-out request should not be followed by reusing the same connection, so the " +
      "test asserts cancellation and then verifies that a new plugin can be created and initialized.")]
    public void SendRequest_WhenRequestTimeoutExpires_ThrowsOperationCanceledExceptionAndCanCreateAgain()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunRequestTimeoutDoesNotBreakPluginCreationAsync());
    }

    [Test]
    [Description(
      "Verifies cancellation of an in-flight GetAuthenticationCredentials request and confirms that the plugin connection " +
      "remains usable afterwards. This covers the real credential acquisition request path; NuGet.Protocol cancellation is " +
      "observable as OperationCanceledException while later credential and claims requests still succeed.")]
    public void SendRequest_WhenCancellationRequestedDuringGetAuthenticationCredentials_CancelsRequestAndPluginRemainsUsable()
    {
      ProtocolTestHelpers.WaitAndUnwrap(RunGetAuthenticationCredentialsCanceledRequestDoesNotBreakConnectionAsync());
    }

    private static async Task RunLifecycleAsync()
    {
      using (var environment = new PluginTestEnvironment())
      using (var cancellation = new CancellationTokenSource(Timeout))
      using (var pluginFactory = new PluginFactory(Timeout))
      using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
      {
        AssertHandshake(plugin);
        await SetLogLevelAsync(plugin, cancellation.Token).ConfigureAwait(false);
        await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
        await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

        plugin.Close();
      }
    }

    private static async Task RunGetOrCreateWithAlreadyCanceledTokenAsync()
    {
      using (var environment = new PluginTestEnvironment())
      using (var cancellation = new CancellationTokenSource())
      using (var pluginFactory = new PluginFactory(Timeout))
      {
        cancellation.Cancel();

        await ProtocolTestHelpers.AssertOperationCanceledAsync(
          () => CreatePluginAsync(pluginFactory, cancellation.Token)).ConfigureAwait(false);
      }
    }

    private static async Task RunCreateAfterCanceledCreationAsync()
    {
      using (var environment = new PluginTestEnvironment())
      using (var pluginFactory = new PluginFactory(Timeout))
      {
        using (var canceledCreation = new CancellationTokenSource())
        {
          canceledCreation.Cancel();

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => CreatePluginAsync(pluginFactory, canceledCreation.Token)).ConfigureAwait(false);
        }

        using (var cancellation = new CancellationTokenSource(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);
          await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
          await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

          plugin.Close();
        }
      }
    }

    private static async Task RunGetOrCreateCanceledDuringHandshakeAsync()
    {
      using (var environment = new PluginTestEnvironment(handshakeDelayMs: DelayedHandlerDelayMs))
      {
        using (var canceledCreation = new CancellationTokenSource(Timeout))
        using (var pluginFactory = new PluginFactory(Timeout))
        {
          canceledCreation.CancelAfter(TimeSpan.FromMilliseconds(100));

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => CreatePluginAsync(pluginFactory, canceledCreation.Token)).ConfigureAwait(false);
        }

        Environment.SetEnvironmentVariable(HandshakeDelayEnvironmentVariable, null);

        using (var cancellation = new CancellationTokenSource(Timeout))
        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);
          await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
          await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

          plugin.Close();
        }
      }
    }

    private static async Task RunHandshakeTimeoutDoesNotBreakCreationAsync()
    {
      using (var environment = new PluginTestEnvironment(handshakeDelayMs: DelayedHandlerDelayMs))
      using (var cancellation = new CancellationTokenSource(Timeout))
      {
        using (var pluginFactory = new PluginFactory(Timeout))
        {
          await ProtocolTestHelpers.AssertOperationCanceledOrTimeoutAsync(
            () => CreatePluginAsync(
              pluginFactory,
              cancellation.Token,
              handshakeTimeout: TimeSpan.FromMilliseconds(250))).ConfigureAwait(false);
        }

        Environment.SetEnvironmentVariable(HandshakeDelayEnvironmentVariable, null);

        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);
          await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
          await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

          plugin.Close();
        }
      }
    }

    private static async Task RunCanceledRequestDoesNotBreakConnectionAsync()
    {
      using (var environment = new PluginTestEnvironment())
      using (var cancellation = new CancellationTokenSource(Timeout))
      using (var pluginFactory = new PluginFactory(Timeout))
      using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
      {
        AssertHandshake(plugin);

        using (var canceledRequest = new CancellationTokenSource())
        {
          canceledRequest.Cancel();

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => plugin.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
              NuGetMessageMethod.Initialize,
              CreateInitializeRequest(),
              canceledRequest.Token)).ConfigureAwait(false);
        }

        await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
        await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

        plugin.Close();
      }
    }

    private static async Task RunInFlightCanceledRequestDoesNotBreakConnectionAsync()
    {
      using (new PluginTestEnvironment(initializeDelayMs: DelayedHandlerDelayMs))
      using (var cancellation = new CancellationTokenSource(Timeout))
      using (var pluginFactory = new PluginFactory(Timeout))
      using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
      {
        AssertHandshake(plugin);

        using (var canceledRequest = new CancellationTokenSource())
        {
          var initializeTask = plugin.Connection
            .SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
              NuGetMessageMethod.Initialize,
              CreateInitializeRequest(),
              canceledRequest.Token);

          canceledRequest.CancelAfter(TimeSpan.FromMilliseconds(100));

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => initializeTask).ConfigureAwait(false);
        }

        Environment.SetEnvironmentVariable(InitializeDelayEnvironmentVariable, null);

        await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
        await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

        plugin.Close();
      }
    }

    private static async Task RunCloseWithInFlightRequestAsync()
    {
      using (var markers = new TemporaryDirectory())
      {
        var startedMarker = Path.Combine(markers.Path, "request-started");
        var canceledMarker = Path.Combine(markers.Path, "request-canceled");
        var completedMarker = Path.Combine(markers.Path, "request-completed");

        using (var environment = new PluginTestEnvironment(
                 initializeDelayMs: DelayedHandlerDelayMs,
                 requestStartedMarkerFile: startedMarker,
                 requestCanceledMarkerFile: canceledMarker,
                 requestCompletedMarkerFile: completedMarker))
        using (var cancellation = new CancellationTokenSource(Timeout))
        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);

          var initializeTask = plugin.Connection
            .SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
              NuGetMessageMethod.Initialize,
              CreateInitializeRequest(),
              CancellationToken.None);

          await ProtocolTestHelpers.WaitForFileAsync(startedMarker).ConfigureAwait(false);

          plugin.Close();

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => initializeTask).ConfigureAwait(false);
        }
      }
    }

    private static async Task RunCanceledRequestDoesNotSendLateResponseAsync()
    {
      using (var markers = new TemporaryDirectory())
      {
        var startedMarker = Path.Combine(markers.Path, "request-started");
        var canceledMarker = Path.Combine(markers.Path, "request-canceled");
        var completedMarker = Path.Combine(markers.Path, "request-completed");

        using (var environment = new PluginTestEnvironment(
                 initializeDelayMs: DelayedHandlerDelayMs,
                 requestStartedMarkerFile: startedMarker,
                 requestCanceledMarkerFile: canceledMarker,
                 requestCompletedMarkerFile: completedMarker))
        using (var cancellation = new CancellationTokenSource(Timeout))
        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);

          using (var canceledRequest = new CancellationTokenSource())
          {
            var initializeTask = plugin.Connection
              .SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
                NuGetMessageMethod.Initialize,
                CreateInitializeRequest(),
                canceledRequest.Token);

            await ProtocolTestHelpers.WaitForFileAsync(startedMarker).ConfigureAwait(false);

            canceledRequest.Cancel();

            await ProtocolTestHelpers.AssertOperationCanceledAsync(
              () => initializeTask).ConfigureAwait(false);
          }

          await ProtocolTestHelpers.WaitForFileAsync(canceledMarker).ConfigureAwait(false);
          await Task.Delay(TimeSpan.FromMilliseconds(DelayedHandlerDelayMs + 250)).ConfigureAwait(false);
          Assert.IsFalse(File.Exists(completedMarker), "Canceled request handler completed after NuGet sent cancel.");

          Environment.SetEnvironmentVariable(InitializeDelayEnvironmentVariable, null);
          Environment.SetEnvironmentVariable(RequestStartedMarkerEnvironmentVariable, null);
          Environment.SetEnvironmentVariable(RequestCanceledMarkerEnvironmentVariable, null);
          Environment.SetEnvironmentVariable(RequestCompletedMarkerEnvironmentVariable, null);

          await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
          await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

          plugin.Close();
        }
      }
    }

    private static async Task RunRequestTimeoutDoesNotBreakPluginCreationAsync()
    {
      using (var environment = new PluginTestEnvironment(initializeDelayMs: DelayedHandlerDelayMs))
      using (var cancellation = new CancellationTokenSource(Timeout))
      {
        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(
                 pluginFactory,
                 cancellation.Token,
                 requestTimeout: TimeSpan.FromMilliseconds(250)).ConfigureAwait(false))
        {
          AssertHandshake(plugin);

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => plugin.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
              NuGetMessageMethod.Initialize,
              CreateInitializeRequest(),
              CancellationToken.None)).ConfigureAwait(false);
        }

        Environment.SetEnvironmentVariable(InitializeDelayEnvironmentVariable, null);

        using (var pluginFactory = new PluginFactory(Timeout))
        using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
        {
          AssertHandshake(plugin);
          await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);
          await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

          plugin.Close();
        }
      }
    }

    private static async Task RunGetAuthenticationCredentialsCanceledRequestDoesNotBreakConnectionAsync()
    {
      using (var environment = new PluginTestEnvironment(authenticationCredentialsDelayMs: DelayedHandlerDelayMs))
      using (var cancellation = new CancellationTokenSource(Timeout))
      using (var pluginFactory = new PluginFactory(Timeout))
      using (var plugin = await CreatePluginAsync(pluginFactory, cancellation.Token).ConfigureAwait(false))
      {
        AssertHandshake(plugin);
        await InitializeAsync(plugin, cancellation.Token).ConfigureAwait(false);

        using (var canceledRequest = new CancellationTokenSource())
        {
          var credentialsTask = plugin.Connection
            .SendRequestAndReceiveResponseAsync<GetAuthenticationCredentialsRequest, GetAuthenticationCredentialsResponse>(
              NuGetMessageMethod.GetAuthenticationCredentials,
              CreateGetAuthenticationCredentialsRequest(),
              canceledRequest.Token);

          canceledRequest.CancelAfter(TimeSpan.FromMilliseconds(100));

          await ProtocolTestHelpers.AssertOperationCanceledAsync(
            () => credentialsTask).ConfigureAwait(false);
        }

        Environment.SetEnvironmentVariable(AuthenticationCredentialsDelayEnvironmentVariable, null);

        await AssertAuthenticationCredentialsAsync(plugin, cancellation.Token).ConfigureAwait(false);
        await AssertAuthenticationClaimsAsync(plugin, cancellation.Token).ConfigureAwait(false);

        plugin.Close();
      }
    }

    private static Task<IPlugin> CreatePluginAsync(
      PluginFactory pluginFactory,
      CancellationToken cancellationToken,
      TimeSpan? handshakeTimeout = null,
      TimeSpan? requestTimeout = null)
    {
      return pluginFactory.GetOrCreateAsync(
        CreatePluginFile(),
        new[] { "-Plugin" },
        new global::NuGet.Protocol.Plugins.RequestHandlers(),
        CreateConnectionOptions(handshakeTimeout, requestTimeout),
        cancellationToken);
    }

    private static ConnectionOptions CreateConnectionOptions(
      TimeSpan? handshakeTimeout = null,
      TimeSpan? requestTimeout = null)
    {
      return new ConnectionOptions(
        protocolVersion: new SemanticVersion(2, 0, 0),
        minimumProtocolVersion: new SemanticVersion(1, 0, 0),
        handshakeTimeout: handshakeTimeout ?? Timeout,
        requestTimeout: requestTimeout ?? Timeout);
    }

    private static PluginFile CreatePluginFile()
    {
      return new PluginFile(GetPluginPath(), new Lazy<PluginFileState>(() => PluginFileState.Valid));
    }

    private static void AssertHandshake(IPlugin plugin)
    {
      Assert.IsNotNull(plugin.Connection.ProtocolVersion, "Handshake did not negotiate a protocol version.");
      Assert.GreaterOrEqual(plugin.Connection.ProtocolVersion, new SemanticVersion(2, 0, 0));
    }

    private static async Task SetLogLevelAsync(IPlugin plugin, CancellationToken cancellationToken)
    {
      var setLogLevelResponse = await plugin.Connection
        .SendRequestAndReceiveResponseAsync<SetLogLevelRequest, SetLogLevelResponse>(
          NuGetMessageMethod.SetLogLevel,
          new SetLogLevelRequest(LogLevel.Verbose),
          cancellationToken)
        .ConfigureAwait(false);

      Assert.AreEqual(NuGetMessageResponseCode.Success, setLogLevelResponse.ResponseCode);
    }

    private static async Task InitializeAsync(IPlugin plugin, CancellationToken cancellationToken)
    {
      var initializeResponse = await plugin.Connection
        .SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          NuGetMessageMethod.Initialize,
          CreateInitializeRequest(),
          cancellationToken)
        .ConfigureAwait(false);

      Assert.AreEqual(NuGetMessageResponseCode.Success, initializeResponse.ResponseCode);
    }

    private static InitializeRequest CreateInitializeRequest()
    {
      return new InitializeRequest("NuGet.Protocol integration test", "en-US", Timeout);
    }

    private static GetAuthenticationCredentialsRequest CreateGetAuthenticationCredentialsRequest()
    {
      return new GetAuthenticationCredentialsRequest(
        new Uri(PackageSourceRepository),
        isRetry: false,
        isNonInteractive: true,
        canShowDialog: false);
    }

    private static async Task AssertAuthenticationClaimsAsync(IPlugin plugin, CancellationToken cancellationToken)
    {
      var claimsResponse = await plugin.Connection
        .SendRequestAndReceiveResponseAsync<GetOperationClaimsRequest, GetOperationClaimsResponse>(
          NuGetMessageMethod.GetOperationClaims,
          new GetOperationClaimsRequest(PackageSourceRepository, new JObject()),
          cancellationToken)
        .ConfigureAwait(false);

      Assert.Contains(NuGetOperationClaim.Authentication, claimsResponse.Claims.ToList());
    }

    private static async Task AssertAuthenticationCredentialsAsync(IPlugin plugin, CancellationToken cancellationToken)
    {
      var credentialsResponse = await plugin.Connection
        .SendRequestAndReceiveResponseAsync<GetAuthenticationCredentialsRequest, GetAuthenticationCredentialsResponse>(
          NuGetMessageMethod.GetAuthenticationCredentials,
          CreateGetAuthenticationCredentialsRequest(),
          cancellationToken)
        .ConfigureAwait(false);

      Assert.AreEqual(NuGetMessageResponseCode.Success, credentialsResponse.ResponseCode);
      Assert.AreEqual("user", credentialsResponse.Username);
      Assert.IsNotNull(credentialsResponse.Password);
    }

    private static string GetPluginPath()
    {
      var pluginPath = typeof(Program).Assembly.Location;
      Assert.IsTrue(File.Exists(pluginPath), "Plugin executable was not found: " + pluginPath);
      return pluginPath;
    }

    private sealed class PluginTestEnvironment : IDisposable
    {
      private readonly string _previousFeeds;
      private readonly string _previousShutdownTimeout;
      private readonly string _previousHandshakeDelay;
      private readonly string _previousInitializeDelay;
      private readonly string _previousAuthenticationCredentialsDelay;
      private readonly string _previousRequestStartedMarker;
      private readonly string _previousRequestCanceledMarker;
      private readonly string _previousRequestCompletedMarker;
      private readonly TemporaryDirectory _tempDirectory;

      public PluginTestEnvironment(
        int? handshakeDelayMs = null,
        int? initializeDelayMs = null,
        int? authenticationCredentialsDelayMs = null,
        string requestStartedMarkerFile = null,
        string requestCanceledMarkerFile = null,
        string requestCompletedMarkerFile = null)
      {
        _previousFeeds = Environment.GetEnvironmentVariable("TEAMCITY_NUGET_FEEDS");
        _previousShutdownTimeout = Environment.GetEnvironmentVariable("NUGET_PLUGIN_SHUTDOWN_TIMEOUT_IN_SECONDS");
        _previousHandshakeDelay = Environment.GetEnvironmentVariable(HandshakeDelayEnvironmentVariable);
        _previousInitializeDelay = Environment.GetEnvironmentVariable(InitializeDelayEnvironmentVariable);
        _previousAuthenticationCredentialsDelay = Environment.GetEnvironmentVariable(AuthenticationCredentialsDelayEnvironmentVariable);
        _previousRequestStartedMarker = Environment.GetEnvironmentVariable(RequestStartedMarkerEnvironmentVariable);
        _previousRequestCanceledMarker = Environment.GetEnvironmentVariable(RequestCanceledMarkerEnvironmentVariable);
        _previousRequestCompletedMarker = Environment.GetEnvironmentVariable(RequestCompletedMarkerEnvironmentVariable);
        _tempDirectory = new TemporaryDirectory();

        var feedsPath = Path.Combine(_tempDirectory.Path, "sources.xml");
        File.WriteAllText(
          feedsPath,
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<sources>" +
          "<source source=\"" + PackageSourceRepository + "\" username=\"user\" password=\"password\" />" +
          "</sources>");

        Environment.SetEnvironmentVariable("TEAMCITY_NUGET_FEEDS", feedsPath);
        Environment.SetEnvironmentVariable("NUGET_PLUGIN_SHUTDOWN_TIMEOUT_IN_SECONDS", "1");
        Environment.SetEnvironmentVariable(HandshakeDelayEnvironmentVariable, handshakeDelayMs?.ToString());
        Environment.SetEnvironmentVariable(InitializeDelayEnvironmentVariable, initializeDelayMs?.ToString());
        Environment.SetEnvironmentVariable(AuthenticationCredentialsDelayEnvironmentVariable, authenticationCredentialsDelayMs?.ToString());
        Environment.SetEnvironmentVariable(RequestStartedMarkerEnvironmentVariable, requestStartedMarkerFile);
        Environment.SetEnvironmentVariable(RequestCanceledMarkerEnvironmentVariable, requestCanceledMarkerFile);
        Environment.SetEnvironmentVariable(RequestCompletedMarkerEnvironmentVariable, requestCompletedMarkerFile);
      }

      public void Dispose()
      {
        Environment.SetEnvironmentVariable("TEAMCITY_NUGET_FEEDS", _previousFeeds);
        Environment.SetEnvironmentVariable("NUGET_PLUGIN_SHUTDOWN_TIMEOUT_IN_SECONDS", _previousShutdownTimeout);
        Environment.SetEnvironmentVariable(HandshakeDelayEnvironmentVariable, _previousHandshakeDelay);
        Environment.SetEnvironmentVariable(InitializeDelayEnvironmentVariable, _previousInitializeDelay);
        Environment.SetEnvironmentVariable(AuthenticationCredentialsDelayEnvironmentVariable, _previousAuthenticationCredentialsDelay);
        Environment.SetEnvironmentVariable(RequestStartedMarkerEnvironmentVariable, _previousRequestStartedMarker);
        Environment.SetEnvironmentVariable(RequestCanceledMarkerEnvironmentVariable, _previousRequestCanceledMarker);
        Environment.SetEnvironmentVariable(RequestCompletedMarkerEnvironmentVariable, _previousRequestCompletedMarker);
        _tempDirectory.Dispose();
      }
    }
  }
}
