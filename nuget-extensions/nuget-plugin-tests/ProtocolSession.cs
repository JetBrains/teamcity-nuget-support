using System;
using System.Threading;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class ProtocolSession : IDisposable
  {
    private ProtocolSession(
      BlockingTextReader reader,
      RecordingTextWriter writer,
      RecordingLogger logger,
      RequestHandlersStub requestHandlers,
      Dispatcher dispatcher,
      Connection connection)
    {
      Reader = reader;
      Writer = writer;
      Logger = logger;
      RequestHandlers = requestHandlers;
      Dispatcher = dispatcher;
      Connection = connection;
    }

    public BlockingTextReader Reader { get; }
    public RecordingTextWriter Writer { get; }
    public RecordingLogger Logger { get; }
    public RequestHandlersStub RequestHandlers { get; private set; }
    public Dispatcher Dispatcher { get; }
    public Connection Connection { get; }

    public static ProtocolSession Create()
    {
      var logger = new RecordingLogger();
      var reader = new BlockingTextReader();
      var writer = new RecordingTextWriter();
      var handlers = new RequestHandlersStub();
      var dispatcher = new Dispatcher(
        handlers,
        new InboundRequestProcessingHandler(Array.Empty<MessageMethod>(), logger),
        logger);
      var connection = new Connection(reader, writer, dispatcher, logger);

      return new ProtocolSession(reader, writer, logger, handlers, dispatcher, connection);
    }

    public static ProtocolSession CreateConnected()
    {
      var session = Create();
      var connectTask = session.Connection.ConnectAsync(CancellationToken.None);

      var handshakeRequest = session.WaitForSingleWrittenMessage();
      session.Reader.AddLine(
        ProtocolTestHelpers.SerializeResponse(handshakeRequest, new HandshakeResponse(MessageResponseCode.Success, null)));

      ProtocolTestHelpers.WaitAndUnwrap(connectTask);
      session.Writer.Clear();
      session.Logger.Clear();

      return session;
    }

    public Message WaitForSingleWrittenMessage()
    {
      Assert.IsTrue(Writer.WaitForLineCount(1, ProtocolTestHelpers.WaitTimeout), "Expected a protocol message to be written.");
      return MessageUtilities.DeserializeMessage(Writer.Lines[0]);
    }

    public Message WaitForWrittenMessage(Predicate<Message> predicate)
    {
      var deadline = DateTime.UtcNow + ProtocolTestHelpers.WaitTimeout;
      while (DateTime.UtcNow < deadline)
      {
        foreach (var line in Writer.Lines)
        {
          var message = MessageUtilities.DeserializeMessage(line);
          if (predicate(message))
          {
            return message;
          }
        }

        Thread.Sleep(10);
      }

      Assert.Fail("Expected protocol message was not written.");
      return null;
    }

    public void Dispose()
    {
      Reader.Complete();
      Connection.Dispose();
      Dispatcher.Dispose();
    }
  }
}
