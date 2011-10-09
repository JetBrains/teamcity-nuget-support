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
import jetbrains.buildServer.nuget.agent.util.fsScanner.*;
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
    final FileSystem fs = m.mock(FileSystem.class);
    final DirectoryEntry root = m.mock(DirectoryEntry.class, "root");
    final DirectoryEntry a = m.mock(DirectoryEntry.class, "a");
    final DirectoryEntry proot = m.mock(DirectoryEntry.class, "proot");
    final DirectoryEntry fsRoot = m.mock(DirectoryEntry.class, "fsRoot");
    final FileEntry c_txt = m.mock(FileEntry.class, "c.txt");

    m.checking(new Expectations(){{
      allowing(fs).caseSensitive(); will(returnValue(true));
      allowing(fs).getRoot(); will(returnValue(fsRoot));
      oneOf(fs).isPathAbsolute("a/c.txt"); will(returnValue(false));

      allowing(fsRoot).getName(); will(returnValue("fsRoot"));
      allowing(fsRoot).getParent(); will(returnValue(null));
      allowing(fsRoot).getSubdirectories(Arrays.asList("root")); will(returnValue(new DirectoryEntry[]{ root} ));

      allowing(root).getParent(); will(returnValue(proot));
      allowing(root).getName(); will(returnValue("root"));
      allowing(root).getSubdirectories(Arrays.asList("a")); will(returnValue(new DirectoryEntry[] { a }));
      allowing(root).getFiles(Arrays.asList("a")); will(returnValue(new FileEntry[0]));
      allowing(proot).getParent(); will(returnValue(null));
      allowing(proot).getName(); will(returnValue("proot"));

      allowing(a).getName(); will(returnValue("a"));
      allowing(a).getFiles(Arrays.asList("c.txt")); will(returnValue(new FileEntry[]{c_txt} ));
      allowing(a).getSubdirectories(Arrays.asList("c.txt")); will(returnValue(new DirectoryEntry[0]));

      allowing(c_txt).getName(); will(returnValue("c.txt"));
      allowing(c_txt).getPath(); will(returnValue(new FileSystemPath("ccc")));
    }});

    DirectoryScanner.findFiles(fs, root, Arrays.asList("a/c.txt"), Collections.<String>emptyList());

    m.assertIsSatisfied();
  }
}
