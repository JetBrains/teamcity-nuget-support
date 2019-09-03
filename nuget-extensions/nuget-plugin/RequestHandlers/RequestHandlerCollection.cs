using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Collections.Generic;
using JetBrains.Annotations;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Represents a collection of NuGet plug-in request handlers.
  /// </summary>
  /// <remarks>This custom collection is used instead of <see cref="JetBrains.TeamCity.NuGet.RequestHandlers"/> because it inherits
  /// <see cref="ConcurrentDictionary{TKey,TValue}"/> which allows for the initializer syntax.</remarks>
  internal class RequestHandlerCollection : IRequestHandlers, IEnumerable<KeyValuePair<MessageMethod, IRequestHandler>>
  {
    private readonly Action<MessageMethod, IRequestHandler> _afterAddHandler;
    private readonly ConcurrentDictionary<MessageMethod, IRequestHandler> _dictionary = new ConcurrentDictionary<MessageMethod, IRequestHandler>();

    public RequestHandlerCollection([NotNull] Action<MessageMethod, IRequestHandler> afterAddHandler)
    {
      _afterAddHandler = afterAddHandler ?? throw new ArgumentNullException(nameof(afterAddHandler));
    }

    public void Add(MessageMethod method, IRequestHandler handler)
    {
      TryAdd(method, handler);
    }

    public bool TryAdd(MessageMethod method, IRequestHandler handler)
    {
      var result =_dictionary.TryAdd(method, handler);
      if (result)
      {
        _afterAddHandler(method, handler);
      }

      return result;
    }

    public void AddOrUpdate(MessageMethod method, Func<IRequestHandler> addHandlerFunc, Func<IRequestHandler, IRequestHandler> updateHandlerFunc)
    {
      IRequestHandler tempAddHandler = null;
      var result = _dictionary.AddOrUpdate(
        method,
        messageMethod =>
        {
          return tempAddHandler = addHandlerFunc();
        },
        (messageMethod, requestHandler) => updateHandlerFunc(requestHandler));

      if (result == tempAddHandler)
      {
        _afterAddHandler(method, result);
      }
    }

    public bool TryGet(MessageMethod method, out IRequestHandler requestHandler)
    {
      return _dictionary.TryGetValue(method, out requestHandler);
    }

    public bool TryRemove(MessageMethod method)
    {
      return _dictionary.TryRemove(method, out IRequestHandler _);
    }

    public IEnumerator<KeyValuePair<MessageMethod, IRequestHandler>> GetEnumerator()
    {
      return _dictionary.GetEnumerator();
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
      return _dictionary.GetEnumerator();
    }
  }
}
