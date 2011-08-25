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

package jetbrains.buildServer.nuget.tests.util.fsScanner;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.runner.publish.fsScanner.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 24.08.11 22:05
 */
public class DirectoryScannerMockFSTest extends BaseTestCase {
  @Test
  public void test_should_not_list_folders_for_known_paths() {
    Mockery m = new Mockery();
    final IFileSystem fs = m.mock(IFileSystem.class);
    final IDirectoryEntry root = m.mock(IDirectoryEntry.class, "root");
    final IDirectoryEntry a = m.mock(IDirectoryEntry.class, "a");
    final IDirectoryEntry proot = m.mock(IDirectoryEntry.class, "proot");
    final IDirectoryEntry fsRoot = m.mock(IDirectoryEntry.class, "fsRoot");
    final IFileEntry c_txt = m.mock(IFileEntry.class, "c.txt");

    m.checking(new Expectations(){{
      allowing(fs).CaseSensitive(); will(returnValue(true));
      allowing(fs).Root(); will(returnValue(fsRoot));
      oneOf(fs).IsPathAbsolute("a/c.txt"); will(returnValue(false));

      allowing(fsRoot).Name(); will(returnValue("fsRoot"));
      allowing(fsRoot).Parent(); will(returnValue(null));
      allowing(fsRoot).Subdirectories(Arrays.asList("root")); will(returnValue(new IDirectoryEntry[]{ root} ));

      allowing(root).Parent(); will(returnValue(proot));
      allowing(root).Name(); will(returnValue("root"));
      allowing(root).Subdirectories(Arrays.asList("a")); will(returnValue(new IDirectoryEntry[] { a }));
      allowing(root).Files(Arrays.asList("a")); will(returnValue(new IFileEntry[0]));
      allowing(proot).Parent(); will(returnValue(null));
      allowing(proot).Name(); will(returnValue("proot"));

      allowing(a).Name(); will(returnValue("a"));
      allowing(a).Files(Arrays.asList("c.txt")); will(returnValue(new IFileEntry[]{c_txt} ));
      allowing(a).Subdirectories(Arrays.asList("c.txt")); will(returnValue(new IDirectoryEntry[0]));

      allowing(c_txt).Name(); will(returnValue("c.txt"));
      allowing(c_txt).Path(); will(returnValue(new FileSystemPath("ccc")));
    }});

    DirectoryScanner.FindFiles(fs, root, Arrays.asList("a/c.txt"), Collections.<String>emptyList());

    m.assertIsSatisfied();
  }
}
