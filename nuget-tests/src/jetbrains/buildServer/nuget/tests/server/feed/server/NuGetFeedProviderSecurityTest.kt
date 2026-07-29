package jetbrains.buildServer.nuget.tests.server.feed.server

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
import org.jmock.AbstractExpectations.returnValue
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Action
import org.jmock.api.Invocation
import org.jmock.lib.legacy.ClassImposteriser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
@TestFor(issues = ["TW-102355"])
class NuGetFeedProviderSecurityTest {

    companion object {
        private const val BUILD_PROJECT = "Child"
        private const val PARENT_PROJECT = "Parent"
        private const val FOREIGN_PROJECT = "Foreign"
        private const val FEED_NAME = "packages"
        private const val OWN_FEED = "$BUILD_PROJECT/$FEED_NAME"
        private const val INHERITED_FEED = "$PARENT_PROJECT/$FEED_NAME"
        private const val FOREIGN_FEED = "$FOREIGN_PROJECT/$FEED_NAME"
        private const val MALFORMED_FEED = "a/b/c"
        private const val REPO_NAME_PARAM = "name"
        private const val INDEX_PACKAGES_PARAM = "indexPackages"
    }

    private lateinit var m: Mockery
    private lateinit var projectManager: ProjectManager
    private lateinit var repositoryManager: RepositoryManager
    private lateinit var repoType: RepositoryType
    private lateinit var build: SBuild
    private lateinit var buildProject: SProject
    private lateinit var parentProject: SProject
    private lateinit var foreignProject: SProject
    private lateinit var feature: SBuildFeatureDescriptor
    private lateinit var provider: NuGetBuildFeedsProviderImpl

    @BeforeMethod
    fun setUp() {
        m = object : Mockery() { init { setImposteriser(ClassImposteriser.INSTANCE) } }
        projectManager = m.mock(ProjectManager::class.java)
        repositoryManager = m.mock(RepositoryManager::class.java)
        repoType = m.mock(RepositoryType::class.java)
        build = m.mock(SBuild::class.java)
        buildProject = m.mock(SProject::class.java, "child")
        parentProject = m.mock(SProject::class.java, "parent")
        foreignProject = m.mock(SProject::class.java, "foreign")
        feature = m.mock(SBuildFeatureDescriptor::class.java)
        provider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)
    }

    fun doesNotIndexIntoUnrelatedProjectFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME)
                stubFeature(FOREIGN_FEED)
                allowing(foreignProject).projectId; will(returnValue(FOREIGN_PROJECT))
                allowing(projectManager).findProjectByExternalId(FOREIGN_PROJECT); will(returnValue(foreignProject))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertFalse(feeds.contains(NuGetFeedData(FOREIGN_PROJECT, FEED_NAME)),
            "Build must not index into a feed of an unrelated project it cannot see")
        Assert.assertTrue(feeds.isEmpty(), "No feed is visible to the build, so none must be indexed")
    }

    fun indexesIntoOwnFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME)
                stubFeature(OWN_FEED)
                allowing(projectManager).findProjectByExternalId(BUILD_PROJECT); will(returnValue(buildProject))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertTrue(feeds.contains(NuGetFeedData(BUILD_PROJECT, FEED_NAME)),
            "A feed in the build's own project must remain indexable")
    }

    fun indexesIntoInheritedAncestorFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProjectWithVisibleFeeds(parentProject to FEED_NAME)
                allowing(parentProject).projectId; will(returnValue(PARENT_PROJECT))
                stubFeature(INHERITED_FEED)
                allowing(projectManager).findProjectByExternalId(PARENT_PROJECT); will(returnValue(parentProject))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertTrue(feeds.contains(NuGetFeedData(PARENT_PROJECT, FEED_NAME)),
            "A feed inherited from an ancestor project must remain indexable")
    }

    fun malformedFeedIdsAreDropped() {
        m.checking(object : Expectations() {
            init {
                stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME)
                stubFeature(MALFORMED_FEED)
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty(), "A malformed feed id [$MALFORMED_FEED] must be dropped")
    }

    fun packgesAreNotIndexesInNonExistingDefaultFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME)
                stubFeature(BUILD_PROJECT)
                allowing(projectManager).findProjectByExternalId(BUILD_PROJECT); will(returnValue(buildProject))
            }
        })

        val feeds = provider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty(), "A default-feed reference with no materialized feed must be dropped")
    }

    private fun Expectations.stubBuildProjectWithVisibleFeeds(vararg specs: Pair<SProject, String>) {
        allowing(build).projectId; will(returnValue(BUILD_PROJECT))
        // 1L - arbitary placeholder, as can be used by any getFeeds(...) (including the ones without permission)
        allowing(build).buildId; will(returnValue(1L))
        allowing(buildProject).projectId; will(returnValue(BUILD_PROJECT))
        allowing(projectManager).findProjectById(BUILD_PROJECT); will(returnValue(buildProject))
        allowing(repositoryManager).getRepositories(buildProject, true); will(feedsFrom(*specs))
    }

    private fun Expectations.stubFeature(feedSelector: String) {
        allowing(build).getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE); will(returnValue(listOf(feature)))
        allowing(feature).parameters; will(returnValue(mapOf(NuGetFeedConstants.NUGET_INDEXER_FEED to feedSelector)))
    }

    private fun feedsFrom(vararg specs: Pair<SProject, String>): Action = object : Action {
        override fun describeTo(description: Description?) = Unit
        override fun invoke(invocation: Invocation?): Any =
            specs.map { (project, name) ->
                NuGetRepository(repoType, project, mapOf(REPO_NAME_PARAM to name, INDEX_PACKAGES_PARAM to "false"))
            }
    }
}
