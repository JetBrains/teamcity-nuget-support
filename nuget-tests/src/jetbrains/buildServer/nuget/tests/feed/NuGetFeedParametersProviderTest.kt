package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.nuget.feed.server.NuGetFeedParametersProvider
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.packages.RepositoryConstants
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.jmock.lib.legacy.ClassImposteriser
import org.testng.Assert
import org.testng.annotations.Test

class NuGetFeedParametersProviderTest {

    @Test
    fun testParameters() {
        val m = object : Mockery() {
            init {
                setImposteriser(ClassImposteriser.INSTANCE)
            }
        }
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val buildType = m.mock(SBuildType::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val repositoryType = m.mock(RepositoryType::class.java)
        val loginConfiguration = m.mock(LoginConfiguration::class.java)
        val projectId = "projectId"

        val parametersProvider = NuGetFeedParametersProvider(
                serverSettings, projectManager, repositoryManager, loginConfiguration
        )

        m.checking(object: Expectations() {
            init {
                allowing(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                allowing(loginConfiguration).isGuestLoginAllowed
                will(returnValue(false))

                allowing(build).buildType
                will(returnValue(buildType))

                allowing(buildType).project
                will(returnValue(project))

                allowing(project).projectId
                will(returnValue(projectId))

                allowing(project).externalId
                will(returnValue(projectId))

                allowing(projectManager).findProjectById(projectId)
                will(returnValue(project))

                allowing(repositoryManager).getRepositories(project, true)
                will(object: CustomAction("") {
                    override fun invoke(invocation: Invocation?): Any {
                        return listOf(NuGetRepository(repositoryType, project,
                                mapOf(RepositoryConstants.REPOSITORY_NAME_KEY to "default")
                        ))
                    }
                })
            }
        })

        val parameters = parametersProvider.getParameters(build, false)

        Assert.assertEquals(parameters, mapOf(
                "teamcity.nuget.feed.httpAuth.projectId.default.v1" to "%teamcity.serverUrl%/httpAuth/app/nuget/feed/projectId/default/v1",
                "teamcity.nuget.feed.httpAuth.projectId.default.v2" to "%teamcity.serverUrl%/httpAuth/app/nuget/feed/projectId/default/v2",
                "teamcity.nuget.feed.httpAuth.projectId.default.v3" to "%teamcity.serverUrl%/httpAuth/app/nuget/feed/projectId/default/v3/index.json",
                "teamcity.nuget.feed.api.key" to ""
        ))
    }
}
