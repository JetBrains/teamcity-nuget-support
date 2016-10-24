/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class DotNetConstantsTest {
    private static final Pattern ourdotNet4_5VersionPattern = Pattern.compile(DotNetConstants.DOTNET4_5VERSION_PATTERN);
    private static final Pattern ourdotNet4VersionPattern = Pattern.compile(DotNetConstants.DOTNET4VERSION_PATTERN);

    @DataProvider(name = "dotNet4_5VersionCases")
    public Object[][] getDotNet4_5VersionCases() {
        return new Object[][] {
                {"DotNetFramework4.5_x86", true},
                {"DotNetFramework4.5.1_x86", true},
                {"DotNetFramework4.5.2_x86", true},
                {"DotNetFramework4.6_x86", true},
                {"DotNetFramework4.6.1_x86", true},
                {"DotNetFramework4.6.2_x86", true},
                {"DotNetFramework4.7_x86", true},
                {"DotNetFramework5.0_x86", true},
                {"DotNetFramework5.0.1_x86", true},
                {"DotNetFramework5.5_x86", true},
                {"DotNetFramework5.5.1_x86", true},
                {"DotNetFramework5.6_x86", true},
                {"DotNetFramework10.0.1_x86", true},
                {"DotNetFramework10.5_x86", true},

                {"DotNetFramework4.5_x64", false},
                {"DotNetFramework4.6.1_x64", false},
                {"DotNetFramework4.0_x86", false},
                {"DotNetFramework4_x86", false},
                {"DotNetFramework4.0_x64", false},
                {"DotNetFramework4_x64", false},
                {"DotNetFramework3.5_x86", false},
                {"DotNetFramework3.0_x86", false},
                {"DotNetFramework2.0_x86", false},
                {"DotNetFramework1.1_x86", false},
                {"DotNetFramework1.0_x86", false},
        };
    }

    @Test(dataProvider = "dotNet4_5VersionCases")
    public void shouldFitDotNet4_5VersionPattern(@NotNull final String dotNetDescription, final boolean expectedFit){
        // Given

        // When
        boolean actualFit = ourdotNet4_5VersionPattern.matcher(dotNetDescription).find();

        // Then
        Assert.assertEquals(actualFit, expectedFit);
    }

    @DataProvider(name = "dotNet4VersionCases")
    public Object[][] getDotNet4VersionCases() {
        return new Object[][] {
                {"DotNetFramework4.0_x86", true},
                {"DotNetFramework4_x86", true},
                {"DotNetFramework4.3_x86", true},
                {"DotNetFramework4.5_x86", true},
                {"DotNetFramework4.5.1_x86", true},
                {"DotNetFramework4.5.2_x86", true},
                {"DotNetFramework4.6_x86", true},
                {"DotNetFramework4.6.1_x86", true},
                {"DotNetFramework4.6.2_x86", true},
                {"DotNetFramework4.7_x86", true},
                {"DotNetFramework5.0_x86", true},
                {"DotNetFramework5.0.1_x86", true},
                {"DotNetFramework5.5_x86", true},
                {"DotNetFramework5.5.1_x86", true},
                {"DotNetFramework5.6_x86", true},
                {"DotNetFramework10.0.1_x86", true},
                {"DotNetFramework10.5_x86", true},

                {"DotNetFramework4.5_x64", false},
                {"DotNetFramework4.0_x64", false},
                {"DotNetFramework4.6.1_x64", false},
                {"DotNetFramework3.5_x86", false},
                {"DotNetFramework3.0_x86", false},
                {"DotNetFramework2.0_x86", false},
                {"DotNetFramework1.1_x86", false},
                {"DotNetFramework1.0_x86", false},
        };
    }

    @Test(dataProvider = "dotNet4VersionCases")
    public void shouldFitDotNet4VersionPattern(@NotNull final String dotNetDescription, final boolean expectedFit){
        // Given

        // When
        boolean actualFit = ourdotNet4VersionPattern.matcher(dotNetDescription).find();

        // Then
        Assert.assertEquals(actualFit, expectedFit);
    }
}