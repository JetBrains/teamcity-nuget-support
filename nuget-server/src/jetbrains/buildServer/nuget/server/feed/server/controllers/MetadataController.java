/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:05
 */
public class MetadataController implements MetadataControllerHandler {
  @NotNull private final MetadataControllersPaths myDescriptor;
  @NotNull private final PackagesWriter myWriter;
  @NotNull private final SecurityContextEx myContext;
  @NotNull private final UserModel myUsers;

  public MetadataController(@NotNull final MetadataControllersPaths descriptor,
                            @NotNull final PackagesWriter writer,
                            @NotNull final SecurityContextEx context,
                            @NotNull final UserModel users) {
    myDescriptor = descriptor;
    myWriter = writer;
    myContext = context;
    myUsers = users;
  }

  @NotNull
  public String getControllerPath() {
    return myDescriptor.getMetadataControllerPath();
  }

  public void processRequest(@NotNull final HttpServletRequest request,
                             @NotNull final HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("utf-8");
    response.setContentType("text/plain");

    final SecurityContextEx.RunAsAction processRequestAction = new SecurityContextEx.RunAsAction() {
      public void run() throws Throwable {
        myWriter.serializePackages(request, response);
      }
    };

    final SUser user = getAssociatedUser(request);
    try {
      myContext.runAs(user, processRequestAction);
    } catch (Exception e) {
      throw e;
    } catch (Throwable th) {
      throw new Exception(th);
    }
  }

  @NotNull
  private SUser getAssociatedUser(@NotNull final HttpServletRequest request) {
    final Long id = parseUserId(request);
    if (id != null) {
      final SUser user = myUsers.findUserById(id);
      if (user != null) return user;
    }
    return myUsers.getGuestUser();
  }

  @Nullable
  private Long parseUserId(@NotNull final HttpServletRequest request) {
    String userId = request.getHeader("X-TeamCity-UserId");
    if (userId == null) return null;
    userId = userId.trim();
    try {
      return Long.parseLong(userId);
    } catch(Exception e) {
      return null;
    }
  }
}
