// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public interface IRequestHandlers
  {
    void AddOrUpdate(MessageMethod method, Func<IRequestHandler> addHandlerFunc, Func<IRequestHandler, IRequestHandler> updateHandlerFunc);
    bool TryAdd(MessageMethod method, IRequestHandler handler);
    bool TryGet(MessageMethod method, out IRequestHandler requestHandler);
    bool TryRemove(MessageMethod method);
  }
}
