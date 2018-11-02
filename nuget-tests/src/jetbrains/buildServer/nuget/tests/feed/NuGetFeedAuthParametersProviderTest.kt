package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.nuget.feed.server.NuGetFeedAuthParametersProvider
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.packages.RepositoryConstants
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.jmock.lib.legacy.ClassImposteriser
import org.testng.annotations.Test

class NuGetFeedAuthParametersProviderTest {

    @Test
    fun testUpdateParameters() {
        val m = object : Mockery() {
            init {
                setImposteriser(ClassImposteriser.INSTANCE)
            }
        }
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val rootUrlHolder = m.mock(RootUrlHolder::class.java)
        val buildStartContext = m.mock(BuildStartContext::class.java)
        val build = m.mock(SRunningBuild::class.java)
        val project = m.mock(SProject::class.java)
        val repositoryType = m.mock(RepositoryType::class.java)
        val buildFeature = m.mock(SBuildFeatureDescriptor::class.java)
        val projectId = "projectId"

        val parametersProvider = NuGetFeedAuthParametersProvider(
                serverSettings, projectManager, repositoryManager, rootUrlHolder
        )

        m.checking(object: Expectations() {
            init {
                allowing(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                allowing(buildStartContext).build
                will(returnValue(build))

                allowing(build).projectId
                will(returnValue(projectId))

                allowing(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(listOf(buildFeature)))

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

                oneOf(rootUrlHolder).rootUrl
                will(returnValue("http://localhost"))

                oneOf(buildStartContext).addSharedParameter("teamcity.nuget.feed.agentSideIndexing", "true")
                oneOf(buildStartContext).addSharedParameter(
                        "system.teamcity.nuget.feed.projectId.default.url",
                        "%teamcity.serverUrl%/httpAuth/app/nuget/feed/projectId/default/"
                )
                oneOf(buildStartContext).addSharedParameter(
                        "system.teamcity.nuget.feed.projectId.default.publicUrl",
                        "http://localhost/httpAuth/app/nuget/feed/projectId/default/"
                )
            }
        })

        parametersProvider.updateParameters(buildStartContext)
    }
}
