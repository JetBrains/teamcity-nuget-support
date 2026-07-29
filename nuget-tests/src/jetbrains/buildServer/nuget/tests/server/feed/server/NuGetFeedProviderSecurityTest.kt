package jetbrains.buildServer.nuget.tests.server.feed.server

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProviderImpl
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.TestFor
import org.hamcrest.Description
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Action
import org.jmock.api.Invocation
import org.jmock.lib.legacy.ClassImposteriser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
class NuGetFeedProviderSecurityTest {

    companion object {
        private const val AVAILABLE_PROJECT = "Available"
        private const val UNAVAILABLE_PROJECT = "Unavailable"
        private const val FEED_NAME = "packages"
        private const val AVAILABLE_FEED = "$AVAILABLE_PROJECT/$FEED_NAME"
        private const val UNAVAILABLE_FEED = "$UNAVAILABLE_PROJECT/$FEED_NAME"
        private const val REPO_NAME_PARAM = "name"
        private const val INDEX_PACKAGES_PARAM = "indexPackages"
    }

    private lateinit var m: Mockery
    private lateinit var projectManager: ProjectManager
    private lateinit var repositoryManager: RepositoryManager
    private lateinit var repoType: RepositoryType
    private lateinit var build: SBuild
    private lateinit var availableProject: SProject
    private lateinit var unavailableProject: SProject
    private lateinit var feature: SBuildFeatureDescriptor
    private lateinit var provider: NuGetBuildFeedsProviderImpl

    @BeforeMethod
    fun setUp() {
        m = object : Mockery() { init { setImposteriser(ClassImposteriser.INSTANCE) } }
        projectManager = m.mock(ProjectManager::class.java)
        repositoryManager = m.mock(RepositoryManager::class.java)
        repoType = m.mock(RepositoryType::class.java)
        build = m.mock(SBuild::class.java)
        availableProject = m.mock(SProject::class.java, "available")
        unavailableProject = m.mock(SProject::class.java, "unavailable")
        feature = m.mock(SBuildFeatureDescriptor::class.java)
        provider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)
    }

    fun doesNotIndexIntoFeedOutsideBuildProjectVisibility() {
        m.checking(object : Expectations() {
            init {
                allowing(build).projectId; will(returnValue(AVAILABLE_PROJECT))
                allowing(availableProject).projectId; will(returnValue(AVAILABLE_PROJECT))
                allowing(unavailableProject).projectId; will(returnValue(UNAVAILABLE_PROJECT))

                allowing(projectManager).findProjectById(AVAILABLE_PROJECT); will(returnValue(availableProject))
                allowing(repositoryManager).getRepositories(availableProject, true); will(availableFeeds())

                allowing(build).getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE); will(returnValue(listOf(feature)))
                allowing(feature).parameters; will(returnValue(mapOf(NuGetFeedConstants.NUGET_INDEXER_FEED to UNAVAILABLE_FEED)))

                allowing(projectManager).findProjectByExternalId(UNAVAILABLE_PROJECT); will(returnValue(unavailableProject))
                allowing(repositoryManager).hasRepository(unavailableProject, PackageConstants.NUGET_PROVIDER_ID, FEED_NAME)
                will(returnValue(true))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertFalse(feeds.contains(NuGetFeedData(UNAVAILABLE_PROJECT, FEED_NAME)),
            "Build must not index into a feed of a project it cannot see")
        Assert.assertTrue(feeds.isEmpty(), "No feed is visible to the build, so none must be indexed")
    }

    fun stillIndexesIntoVisibleOwnFeed() {
        m.checking(object : Expectations() {
            init {
                allowing(build).projectId; will(returnValue(AVAILABLE_PROJECT))
                allowing(availableProject).projectId; will(returnValue(AVAILABLE_PROJECT))
                allowing(projectManager).findProjectById(AVAILABLE_PROJECT); will(returnValue(availableProject))
                allowing(repositoryManager).getRepositories(availableProject, true); will(availableFeeds())
                allowing(build).getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE); will(returnValue(listOf(feature)))
                allowing(feature).parameters; will(returnValue(mapOf(NuGetFeedConstants.NUGET_INDEXER_FEED to AVAILABLE_FEED)))
                allowing(projectManager).findProjectByExternalId(AVAILABLE_PROJECT); will(returnValue(availableProject))
                allowing(repositoryManager).hasRepository(availableProject, PackageConstants.NUGET_PROVIDER_ID, FEED_NAME)
                will(returnValue(true))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertTrue(feeds.contains(NuGetFeedData(AVAILABLE_PROJECT, FEED_NAME)),
            "A feed visible to the build's own project must remain indexable")
    }

    private fun availableFeeds(): Action = object : Action {
        override fun describeTo(description: Description?) = Unit
        override fun invoke(invocation: Invocation?): Any =
            listOf(NuGetRepository(repoType, availableProject, mapOf(REPO_NAME_PARAM to FEED_NAME, INDEX_PACKAGES_PARAM to "false")))
    }
}
