

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.BDDAssertions.then;

public class DotNetConstantsTest {
    private static final Pattern ourMonoVersionPattern = Pattern.compile(DotNetConstants.MONO_PATH);
    private static final Pattern ourdotNet4_5VersionPattern = Pattern.compile(DotNetConstants.DOTNET4_5VERSION_PATTERN);
    private static final Pattern ourdotNet4VersionPattern = Pattern.compile(DotNetConstants.DOTNET4VERSION_PATTERN);

    @DataProvider(name = "monoVersionCases")
    public Object[][] getMonoVersionCases() {
        return new Object[][] {
                {"Mono_Path", true},
                {"NotMono_Path", true},
        };
    }

    @Test(dataProvider = "monoVersionCases")
    public void shouldFitMonoVersionPattern(@NotNull final String monoDescription, final boolean expectedFit){
        // Given

        // When
        boolean actualFit = ourMonoVersionPattern.matcher(monoDescription).find();

        // Then
        then(actualFit).isEqualTo(expectedFit);
    }

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
        then(actualFit).isEqualTo(expectedFit);
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
        then(actualFit).isEqualTo(expectedFit);
    }
}
