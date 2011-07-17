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

package jetbrains.buildServer.nuget.tests.feed;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.EntitiesResponse;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 1:03
 */
public class FeedTest extends BaseTestCase {

  private void doTest(@NotNull final EntitiesResponse responce, @NotNull final String gold) {



  }


  @NotNull
  private File getTestDataPath(String prefix) {
    return FileUtil.getCanonicalFile(new File("./nuget-tests/testData/feed", prefix));
  }
}
