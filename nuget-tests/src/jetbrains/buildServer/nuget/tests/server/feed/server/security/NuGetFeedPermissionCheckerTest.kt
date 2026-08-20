package jetbrains.buildServer.nuget.tests.server.feed.server.security

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.security.NuGetFeedPermissionCheckerImpl
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.util.TestFor
import org.hamcrest.Description
import org.jmock.AbstractExpectations
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
class NuGetFeedPermissionCheckerTest {

    companion object {
        private const val BUILD_PROJECT = "Child"
        private const val PARENT_PROJECT = "Parent"
        private const val FOREIGN_PROJECT = "Foreign"
        private const val DESCENDANT_PROJECT = "GrandChild"
        private const val FEED_NAME = "packages"
        private const val REPO_NAME_PARAM = "name"
        private const val INDEX_PACKAGES_PARAM = "indexPackages"
    }

    private lateinit var m: Mockery
    private lateinit var projectManager: ProjectManager
    private lateinit var repositoryManager: RepositoryManager
    private lateinit var repoType: RepositoryType
    private lateinit var build: SBuild
    private lateinit var buildProject: SProject
    private lateinit var checker: NuGetFeedPermissionCheckerImpl

    @BeforeMethod
    fun setUp() {
        m = object : Mockery() { init { setImposteriser(ClassImposteriser.INSTANCE) } }
        projectManager = m.mock(ProjectManager::class.java)
        repositoryManager = m.mock(RepositoryManager::class.java)
        repoType = m.mock(RepositoryType::class.java)
        build = m.mock(SBuild::class.java)
        buildProject = m.mock(SProject::class.java, "child")
        checker = NuGetFeedPermissionCheckerImpl(projectManager, repositoryManager)
    }

    fun canWriteAllowsOwnFeed() {
        m.checking(object : Expectations() { init { stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME) } })
        Assert.assertTrue(checker.canWrite(build, NuGetFeedData(BUILD_PROJECT, BUILD_PROJECT, FEED_NAME)))
    }

    fun canWriteDeniesUnrelatedFeed() {
        m.checking(object : Expectations() { init { stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME) } })
        Assert.assertFalse(checker.canWrite(build, NuGetFeedData(FOREIGN_PROJECT, FOREIGN_PROJECT, FEED_NAME)))
    }

    fun canWriteDeniesDescendantFeed() {
        m.checking(object : Expectations() { init { stubBuildProjectWithVisibleFeeds(buildProject to FEED_NAME) } })
        Assert.assertFalse(checker.canWrite(build, NuGetFeedData(DESCENDANT_PROJECT, DESCENDANT_PROJECT, FEED_NAME)))
    }

    fun canWriteDeniesWhenBuildProjectMissing() {
        m.checking(object : Expectations() {
            init {
                allowing(build).projectId; will(returnValue(BUILD_PROJECT))
                allowing(projectManager).findProjectById(BUILD_PROJECT); will(returnValue(null))
            }
        })
        Assert.assertFalse(checker.canWrite(build, NuGetFeedData(PARENT_PROJECT, PARENT_PROJECT, FEED_NAME)))
    }

    private fun Expectations.stubBuildProjectWithVisibleFeeds(vararg specs: Pair<SProject, String>) {
        allowing(build).projectId; will(AbstractExpectations.returnValue(BUILD_PROJECT))
        allowing(buildProject).projectId; will(AbstractExpectations.returnValue(BUILD_PROJECT))
        allowing(projectManager).findProjectById(BUILD_PROJECT); will(AbstractExpectations.returnValue(buildProject))
        allowing(repositoryManager).getRepositories(buildProject, true); will(feedsFrom(*specs))
    }

    private fun feedsFrom(vararg specs: Pair<SProject, String>): Action = object : Action {
        override fun describeTo(description: Description?) = Unit
        override fun invoke(invocation: Invocation?): Any =
            specs.map { (project, name) ->
                NuGetRepository(repoType, project, mapOf(REPO_NAME_PARAM to name, INDEX_PACKAGES_PARAM to "false"))
            }
    }
}
