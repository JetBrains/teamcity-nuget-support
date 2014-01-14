/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed;

import org.jetbrains.annotations.NotNull;

/**
 * Created 02.01.13 17:33
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class FeedCredentials {
  private final String myUser;
  private final String myPassword;

  public FeedCredentials(@NotNull String user,
                         @NotNull String password) {
    myUser = user;
    myPassword = password;
  }

  @NotNull
  public String getUsername() {
    return myUser;
  }

  @NotNull
  public String getPassword() {
    return myPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeedCredentials that = (FeedCredentials) o;

    if (!myPassword.equals(that.myPassword)) return false;
    if (!myUser.equals(that.myUser)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    return result;
  }
}
