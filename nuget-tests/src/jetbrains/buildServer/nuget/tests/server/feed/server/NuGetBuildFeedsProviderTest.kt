package jetbrains.buildServer.nuget.tests.server.feed.server

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProviderImpl
import jetbrains.buildServer.nuget.feed.server.index.impl.security.NuGetFeedPermissionChecker
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.TestFor
import org.hamcrest.Description
import org.jmock.AbstractExpectations
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
class NuGetBuildFeedsProviderTest : BaseTestCase() {

    companion object {
        private const val BUILD_PROJECT = "Child"
        private const val FOREIGN_PROJECT = "Foreign"
        private const val FEED_NAME = "packages"
        private const val OWN_FEED = "$BUILD_PROJECT/$FEED_NAME"
        private const val FOREIGN_FEED = "$FOREIGN_PROJECT/$FEED_NAME"
        private const val ILLEGAL_FEED_SELECTOR = "a/b/c"
        private const val REPO_NAME_PARAM = "name"
        private const val INDEX_PACKAGES_PARAM = "indexPackages"
    }

    private lateinit var m: Mockery
    private lateinit var projectManager: ProjectManager
    private lateinit var repositoryManager: RepositoryManager
    private lateinit var repoType: RepositoryType
    private lateinit var build: SBuild
    private lateinit var buildProject: SProject
    private lateinit var foreignProject: SProject
    private lateinit var feature: SBuildFeatureDescriptor
    private lateinit var permissionChecker: NuGetFeedPermissionChecker
    private lateinit var provider: NuGetBuildFeedsProviderImpl

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        m = object : Mockery() { init { setImposteriser(ClassImposteriser.INSTANCE) } }
        projectManager = m.mock(ProjectManager::class.java)
        repositoryManager = m.mock(RepositoryManager::class.java)
        repoType = m.mock(RepositoryType::class.java)
        build = m.mock(SBuild::class.java)
        buildProject = m.mock(SProject::class.java, "child")
        foreignProject = m.mock(SProject::class.java, "foreign")
        feature = m.mock(SBuildFeatureDescriptor::class.java)
        permissionChecker = m.mock(NuGetFeedPermissionChecker::class.java)
        provider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager, permissionChecker)
    }

    fun resolveAcceptsWritableFeatureFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(OWN_FEED)
                allowing(projectManager).findProjectByExternalId(BUILD_PROJECT); will(returnValue(buildProject))
                stubWritableFeeds(NuGetFeedData(BUILD_PROJECT, FEED_NAME))
            }
        })

        val result = provider.resolveIndexerFeeds(build)

        Assert.assertTrue(result.accessible.contains(NuGetFeedData(BUILD_PROJECT, FEED_NAME)))
        Assert.assertTrue(result.rejected.isEmpty())
    }

    fun resolveRejectsUnwritableFeatureFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(FOREIGN_FEED)
                allowing(foreignProject).projectId; will(returnValue(FOREIGN_PROJECT))
                allowing(projectManager).findProjectByExternalId(FOREIGN_PROJECT); will(returnValue(foreignProject))
                stubWritableFeeds()
            }
        })

        val result = provider.resolveIndexerFeeds(build)

        Assert.assertTrue(result.accessible.isEmpty())
        Assert.assertTrue(result.rejected.contains(FOREIGN_FEED))
    }

    fun resolveAcceptsUnwritableFeatureFeedWhenCrossProjectAccessEnabled() {
        setInternalProperty(NuGetFeedConstants.PROP_NUGET_FEED_ENABLE_CROSS_PROJECT_ACCESS, true)
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(FOREIGN_FEED)
                allowing(foreignProject).projectId; will(returnValue(FOREIGN_PROJECT))
                allowing(projectManager).findProjectByExternalId(FOREIGN_PROJECT); will(returnValue(foreignProject))
                allowing(repositoryManager).hasRepository(foreignProject, PackageConstants.NUGET_PROVIDER_ID, FEED_NAME)
                will(returnValue(true))
                stubWritableFeeds()
            }
        })

        val result = provider.resolveIndexerFeeds(build)

        Assert.assertTrue(result.accessible.contains(NuGetFeedData(FOREIGN_PROJECT, FEED_NAME)))
        Assert.assertTrue(result.rejected.isEmpty())
    }

    fun resolveDropsMalformedFeedId() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(ILLEGAL_FEED_SELECTOR)
                stubWritableFeeds()
            }
        })

        val result = provider.resolveIndexerFeeds(build)

        Assert.assertTrue(result.accessible.isEmpty())
        Assert.assertTrue(result.rejected.isEmpty())
    }

    fun resolveRejectsFeedWhenProjectDoesNotExist() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(FOREIGN_FEED)
                allowing(projectManager).findProjectByExternalId(FOREIGN_PROJECT); will(returnValue(null))
                stubWritableFeeds()
            }
        })

        val result = provider.resolveIndexerFeeds(build)

        Assert.assertTrue(result.rejected.contains(FOREIGN_FEED))
    }

    fun resolveIncludesImplicitlyIndexedFeed() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos("impl"))
                allowing(build).getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE); will(returnValue(emptyList<SBuildFeatureDescriptor>()))
                stubWritableFeeds(NuGetFeedData(BUILD_PROJECT, "impl"))
            }
        })

        Assert.assertTrue(provider.resolveIndexerFeeds(build).accessible.contains(NuGetFeedData(BUILD_PROJECT, "impl")))
    }

    fun getFeedsReturnsAccessibleFeeds() {
        m.checking(object : Expectations() {
            init {
                stubBuildProject()
                allowing(repositoryManager).getRepositories(buildProject, true); will(indexedRepos())
                stubFeature(OWN_FEED)
                allowing(projectManager).findProjectByExternalId(BUILD_PROJECT); will(returnValue(buildProject))
                stubWritableFeeds(NuGetFeedData(BUILD_PROJECT, FEED_NAME))
            }
        })

        Assert.assertTrue(provider.getFeeds(build).contains(NuGetFeedData(BUILD_PROJECT, FEED_NAME)))
    }

    private fun Expectations.stubBuildProject() {
        allowing(build).projectId; will(AbstractExpectations.returnValue(BUILD_PROJECT))
        allowing(buildProject).projectId; will(AbstractExpectations.returnValue(BUILD_PROJECT))
        allowing(projectManager).findProjectById(BUILD_PROJECT); will(AbstractExpectations.returnValue(buildProject))
    }

    private fun Expectations.stubWritableFeeds(vararg feeds: NuGetFeedData) {
        allowing(permissionChecker).getWritableFeeds(buildProject); will(AbstractExpectations.returnValue(feeds.toSet()))
    }

    private fun Expectations.stubFeature(feedSelector: String) {
        allowing(build).getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE); will(AbstractExpectations.returnValue(listOf(feature)))
        allowing(feature).parameters; will(AbstractExpectations.returnValue(mapOf(NuGetFeedConstants.NUGET_INDEXER_FEED to feedSelector)))
    }

    private fun indexedRepos(vararg names: String): Action = object : Action {
        override fun describeTo(description: Description?) = Unit
        override fun invoke(invocation: Invocation?): Any =
            names.map { NuGetRepository(repoType, buildProject, mapOf(REPO_NAME_PARAM to it, INDEX_PACKAGES_PARAM to "true")) }
    }
}
