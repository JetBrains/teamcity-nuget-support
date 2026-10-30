using System;
using System.Collections.Generic;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class RequestHandlersStub : IRequestHandlers
  {
    private readonly Dictionary<MessageMethod, IRequestHandler> _handlers =
      new Dictionary<MessageMethod, IRequestHandler>();

    public void AddOrUpdate(
      MessageMethod method,
      Func<IRequestHandler> addHandlerFunc,
      Func<IRequestHandler, IRequestHandler> updateHandlerFunc)
    {
      _handlers[method] = _handlers.TryGetValue(method, out var handler)
        ? updateHandlerFunc(handler)
        : addHandlerFunc();
    }

    public bool TryAdd(MessageMethod method, IRequestHandler handler)
    {
      if (_handlers.ContainsKey(method))
      {
        return false;
      }

      _handlers.Add(method, handler);
      return true;
    }

    public bool TryGet(MessageMethod method, out IRequestHandler requestHandler)
    {
      return _handlers.TryGetValue(method, out requestHandler);
    }

    public bool TryRemove(MessageMethod method)
    {
      return _handlers.Remove(method);
    }
  }
}
