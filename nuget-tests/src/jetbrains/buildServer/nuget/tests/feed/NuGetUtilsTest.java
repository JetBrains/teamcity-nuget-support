/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.TestFor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Dmitry.Tretyakov
 *         Date: 29.08.2016
 *         Time: 16:38
 */
public class NuGetUtilsTest {
    @Test
    @TestFor(issues = "TW-43254")
    public void test_getRegularValue() {
        final Map<String, String> attributes = CollectionsUtil.asMap("Deps", "1");
        final String value = NuGetUtils.getValue(attributes, "Deps");
        Assert.assertEquals(value, "1");
    }

    @Test
    @TestFor(issues = "TW-43254")
    public void test_getCombinedValue() {
        final Map<String, String> attributes = CollectionsUtil.asMap(
                "Deps", "1",
                "Deps1", "2",
                "Deps2", "3",
                "Deps5", "5");
        final String value = NuGetUtils.getValue(attributes, "Deps");
        Assert.assertEquals(value, "123");
    }
}
