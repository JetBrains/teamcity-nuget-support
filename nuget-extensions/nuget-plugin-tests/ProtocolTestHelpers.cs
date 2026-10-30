using System;
using System.Diagnostics;
using System.IO;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using NUnit.Framework;
using NuGetPluginException = NuGet.Protocol.Plugins.PluginException;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal static class ProtocolTestHelpers
  {
    public static readonly TimeSpan WaitTimeout = TimeSpan.FromSeconds(30);

    public static string SerializeResponse<TPayload>(Message request, TPayload payload) where TPayload : class
    {
      return MessageUtilities.SerializeMessage(
        MessageUtilities.Create(request.RequestId, MessageType.Response, request.Method, payload));
    }

    public static string SerializeFault(Message request, string message)
    {
      return MessageUtilities.SerializeMessage(
        MessageUtilities.Create(request.RequestId, MessageType.Fault, request.Method, new Fault(message)));
    }

    public static void WaitAndUnwrap(Task task)
    {
      try
      {
        if (!task.Wait(WaitTimeout))
        {
          Assert.Fail("Task did not complete in time.");
        }
      }
      catch (AggregateException ex)
      {
        throw ex.InnerExceptions.Count == 1 ? ex.InnerException : ex;
      }
    }

    public static T WaitAndUnwrap<T>(Task<T> task)
    {
      WaitAndUnwrap((Task)task);
      return task.Result;
    }

    public static async Task WaitForFileAsync(string path)
    {
      var timer = Stopwatch.StartNew();
      while (timer.Elapsed < WaitTimeout)
      {
        if (File.Exists(path))
        {
          return;
        }

        await Task.Delay(TimeSpan.FromMilliseconds(50)).ConfigureAwait(false);
      }

      Assert.Fail("File was not created in time: " + path);
    }

    public static void AssertOperationCanceled(Task task)
    {
      try
      {
        WaitAndUnwrap(task);
        Assert.Fail("OperationCanceledException was expected.");
      }
      catch (OperationCanceledException)
      {
      }
    }


    public static async Task AssertOperationCanceledAsync(Func<Task> action)
    {
      try
      {
        await action().ConfigureAwait(false);
        Assert.Fail("OperationCanceledException was expected.");
      }
      catch (Exception ex) when (IsOperationCanceled(ex))
      {
      }
    }

    public static async Task AssertOperationCanceledOrTimeoutAsync(Func<Task> action)
    {
      try
      {
        await action().ConfigureAwait(false);
        Assert.Fail("OperationCanceledException or TimeoutException was expected.");
      }
      catch (Exception ex) when (IsOperationCanceledOrTimeout(ex))
      {
      }
    }

    private static bool IsOperationCanceled(Exception ex)
    {
      if (ex is OperationCanceledException)
      {
        return true;
      }

      return ex is NuGetPluginException pluginException &&
             pluginException.InnerException != null &&
             IsOperationCanceled(pluginException.InnerException);
    }

    private static bool IsOperationCanceledOrTimeout(Exception ex)
    {
      if (ex is TimeoutException || IsOperationCanceled(ex))
      {
        return true;
      }

      return ex is NuGetPluginException pluginException &&
             pluginException.InnerException != null &&
             IsOperationCanceledOrTimeout(pluginException.InnerException);
    }

    public static IDisposable ChangeRequestTimeout(TimeSpan timeout)
    {
      var previousHandshakeTimeout = PluginTimeouts.Instance.HandshakeTimeout;
      var previousRequestTimeout = PluginTimeouts.Instance.RequestTimeout;

      PluginTimeouts.SetTimeouts(previousHandshakeTimeout, timeout);

      return new DelegateDisposable(
        delegate { PluginTimeouts.SetTimeouts(previousHandshakeTimeout, previousRequestTimeout); });
    }
  }
}
