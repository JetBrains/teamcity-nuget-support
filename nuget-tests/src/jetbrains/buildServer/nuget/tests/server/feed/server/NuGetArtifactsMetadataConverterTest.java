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

package jetbrains.buildServer.nuget.tests.server.feed.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetArtifactsMetadataConverter;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.cleanup.ServerCleanupManager;
import jetbrains.buildServer.serverSide.db.SQLRunnerEx;
import jetbrains.buildServer.serverSide.metadata.impl.MetadataStorageEx;
import jetbrains.buildServer.serverSide.metadata.impl.indexer.MetadataIndexerService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetArtifactsMetadataConverterTest extends BaseTestCase {

  private Mockery m;
  private NuGetArtifactsMetadataConverter myConverter;
  private MetadataStorageEx myMetadataStorage;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();

    m = new Mockery();

    myMetadataStorage = m.mock(MetadataStorageEx.class);
    final MetadataIndexerService indexerService = new MetadataIndexerService(
            m.mock(ServerCleanupManager.class),
            m.mock(SQLRunnerEx.class),
            myMetadataStorage,
            m.mock(ExtensionHolder.class),
            m.mock(BuildsManager.class));

    final File tempDir = createTempDir();
    myConverter = new NuGetArtifactsMetadataConverter(indexerService, tempDir);
  }

  @Test
  public void should_not_run_conversion_twice() throws Exception {
    m.checking(new Expectations() {{
      one(myMetadataStorage).getIndexedBuilds("nuget");
      will(returnValue(Collections.emptySet()));
    }});

    myConverter.doConversion();
    myConverter.doConversion();

    m.assertIsSatisfied();
  }
}
