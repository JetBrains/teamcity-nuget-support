using System;
using System.Threading;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  [TestFixture]
  public class DispatcherTest
  {
    [Test]
    public void SendRequestAndReceiveResponseAsync_ReturnsResponsePayload()
    {
      using (var session = ProtocolSession.CreateConnected())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          CancellationToken.None);

        var request = session.WaitForSingleWrittenMessage();
        session.Reader.AddLine(ProtocolTestHelpers.SerializeResponse(request, new InitializeResponse(MessageResponseCode.Success)));

        var response = ProtocolTestHelpers.WaitAndUnwrap(task);

        Assert.AreEqual(MessageResponseCode.Success, response.ResponseCode);
      }
    }

    [Test]
    public void SendRequestAndReceiveResponseAsync_WhenFaultResponseReceived_ThrowsProtocolException()
    {
      using (var session = ProtocolSession.CreateConnected())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          CancellationToken.None);

        var request = session.WaitForSingleWrittenMessage();
        session.Reader.AddLine(ProtocolTestHelpers.SerializeFault(request, "boom"));

        var exception = Assert.Throws<ProtocolException>(delegate { ProtocolTestHelpers.WaitAndUnwrap(task); });
        Assert.AreEqual("boom", exception.Message);
      }
    }

    [Test]
    public void SendRequestAndReceiveResponseAsync_WhenCancellationTokenCanceled_SendsCancelAndCancelsTask()
    {
      using (var session = ProtocolSession.CreateConnected())
      using (var cancellation = new CancellationTokenSource())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          cancellation.Token);

        var request = session.WaitForSingleWrittenMessage();
        cancellation.Cancel();

        ProtocolTestHelpers.AssertOperationCanceled(task);

        var cancel = session.WaitForWrittenMessage(message => message.RequestId == request.RequestId &&
                                                              message.Type == MessageType.Cancel &&
                                                              message.Method == MessageMethod.Initialize);

        Assert.AreEqual(request.RequestId, cancel.RequestId);
      }
    }

    [Test]
    public void SendRequestAndReceiveResponseAsync_WhenRequestTimesOut_SendsCancelAndCancelsTask()
    {
      using (ProtocolTestHelpers.ChangeRequestTimeout(TimeSpan.FromMilliseconds(50)))
      using (var session = ProtocolSession.CreateConnected())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          CancellationToken.None);

        var request = session.WaitForSingleWrittenMessage();

        ProtocolTestHelpers.AssertOperationCanceled(task);

        var cancel = session.WaitForWrittenMessage(message => message.RequestId == request.RequestId &&
                                                              message.Type == MessageType.Cancel &&
                                                              message.Method == MessageMethod.Initialize);

        Assert.AreEqual(request.RequestId, cancel.RequestId);
      }
    }

    [Test]
    public void endRequestAndReceiveResponseAsync_WhenNuGetCliAcknowledgesCanceledRequest_KeepsTaskCanceled()
    {
      using (var session = ProtocolSession.CreateConnected())
      using (var cancellation = new CancellationTokenSource())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          cancellation.Token);

        var request = session.WaitForSingleWrittenMessage();
        cancellation.Cancel();

        ProtocolTestHelpers.AssertOperationCanceled(task);

        session.Dispatcher.HandleIncomingMessage(new Message(request.RequestId, MessageType.Cancel, request.Method));

        ProtocolTestHelpers.AssertOperationCanceled(task);
      }
    }

    [Test]
    public void SendRequestAndReceiveResponseAsync_WhenNuGetCliSendsUnexpectedCancel_ThrowsProtocolException()
    {
      using (var session = ProtocolSession.CreateConnected())
      {
        var task = session.Connection.SendRequestAndReceiveResponseAsync<InitializeRequest, InitializeResponse>(
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)),
          CancellationToken.None);

        var request = session.WaitForSingleWrittenMessage();

        session.Dispatcher.HandleIncomingMessage(new Message(request.RequestId, MessageType.Cancel, request.Method));

        var exception = Assert.Throws<ProtocolException>(delegate { ProtocolTestHelpers.WaitAndUnwrap(task); });
        Assert.AreEqual("Invalid cancel response.", exception.Message);
      }
    }

    [Test]
    public void HandleIncomingMessage_WhenNuGetCliCancelsInboundRequest_CancelsHandlerAndSendsCancelResponse()
    {
      using (var session = ProtocolSession.CreateConnected())
      {
        var handler = new CancelAwareRequestHandler();
        session.RequestHandlers.TryAdd(MessageMethod.Initialize, handler);

        var request = MessageUtilities.Create(
          "cli-request-1",
          MessageType.Request,
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)));

        session.Dispatcher.HandleIncomingMessage(request);
        Assert.IsTrue(handler.Started.WaitOne(ProtocolTestHelpers.WaitTimeout), "Inbound request handler was not started.");

        session.Dispatcher.HandleIncomingMessage(new Message(request.RequestId, MessageType.Cancel, request.Method));

        Assert.IsTrue(handler.Canceled.WaitOne(ProtocolTestHelpers.WaitTimeout), "Inbound request handler did not observe cancellation.");

        var cancel = session.WaitForWrittenMessage(message => message.RequestId == request.RequestId &&
                                                              message.Type == MessageType.Cancel &&
                                                              message.Method == MessageMethod.Initialize);

        Assert.AreEqual(request.RequestId, cancel.RequestId);
      }
    }
  }
}
