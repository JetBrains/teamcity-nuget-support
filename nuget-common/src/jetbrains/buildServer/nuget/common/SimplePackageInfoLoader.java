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

package jetbrains.buildServer.nuget.common;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 16:34
 */
public class SimplePackageInfoLoader extends PackageInfoLoaderBase {
  @NotNull
  public PackageInfo loadPackageInfo(@NotNull final File pkg) throws PackageLoadException {
    return loadPackage(
            new PackageHolder() {
              @NotNull
              public String getPackageName() {
                return pkg.getPath();
              }

              @NotNull
              public InputStream openPackage() throws IOException, PackageLoadException {
                return new FileInputStream(pkg);
              }
            },
            new PackageInfoLoader<PackageInfo>() {
              @NotNull
              public PackageInfo createPackageInfo(@NotNull Element nuspec, @NotNull String id, @NotNull String version) throws PackageLoadException {
                return new PackageInfo(id, version);
              }
            }
    );
  }
}
