using System;
using System.Linq;
using System.Threading;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  [TestFixture]
  public class ConnectionTest
  {
    [Test]
    public void SendAsync_WhenMessageIsNull_ThrowsArgumentNullException()
    {
      using (var session = ProtocolSession.Create())
      {
        Assert.Throws<ArgumentNullException>(() =>
                                             {
                                               ProtocolTestHelpers.WaitAndUnwrap(
                                                 session.Connection.SendAsync(null, CancellationToken.None));
                                             });
      }
    }

    [Test]
    public void SendAsync_WhenConnectionIsNotStarted_ThrowsInvalidOperationException()
    {
      using (var session = ProtocolSession.Create())
      {
        var request = MessageUtilities.Create(
          "1",
          MessageType.Request,
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)));

        Assert.Throws<InvalidOperationException>(() =>
                                                 {
                                                   ProtocolTestHelpers.WaitAndUnwrap(
                                                     session.Connection.SendAsync(request, CancellationToken.None));
                                                 });
      }
    }

    [Test]
    public void SendAsync_WritesSerializedMessageAndFlushes()
    {
      using (var session = ProtocolSession.CreateConnected())
      {
        var request = MessageUtilities.Create(
          "request-1",
          MessageType.Request,
          MessageMethod.Initialize,
          new InitializeRequest("client", "en-US", TimeSpan.FromSeconds(1)));

        ProtocolTestHelpers.WaitAndUnwrap(session.Connection.SendAsync(request, CancellationToken.None));

        Assert.AreEqual(1, session.Writer.Lines.Count);
        Assert.AreEqual(1, session.Writer.FlushCount);

        var writtenMessage = MessageUtilities.DeserializeMessage(session.Writer.Lines[0]);
        Assert.AreEqual("request-1", writtenMessage.RequestId);
        Assert.AreEqual(MessageType.Request, writtenMessage.Type);
        Assert.AreEqual(MessageMethod.Initialize, writtenMessage.Method);
      }
    }
  }
}
