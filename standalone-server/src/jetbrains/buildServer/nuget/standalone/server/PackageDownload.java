/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.standalone.server;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.02.12 1:34
 */
@Path("download/{package}")
public class PackageDownload {
  @NotNull
  private final File myRoot;

  public PackageDownload(@NotNull final File packagesRoot) {
    myRoot = packagesRoot;
  }

  @GET
  public Response processDownload(@PathParam("package") String param) throws FileNotFoundException {
    if (param == null
            || StringUtil.isEmptyOrSpaces(param)
            || param.contains("..")
            || param.contains("/")
            || param.contains("\\")) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid path").build();
    }

    final File res = new File(myRoot, param + ".nupkg");
    if (!res.isFile()) {
      return Response.status(Response.Status.NOT_FOUND).entity("Package not found.").build();
    }

    return Response.ok().type("application/zip").entity(new FileInputStream(res)).build();
  }

}
