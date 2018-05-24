package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProviderImpl
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.RepositoryType
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import org.hamcrest.Description
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Action
import org.jmock.api.Invocation
import org.testng.Assert
import org.testng.annotations.Test
import org.jmock.lib.legacy.ClassImposteriser

@Test
class NuGetBuildFeedsProviderTest {

    fun getEmptyListIfProjectNotFound() {
        val m = Mockery()
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                oneOf(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(null))

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(emptyList<SBuildFeatureDescriptor>()))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty())
        m.assertIsSatisfied()
    }

    fun getEmptyListIfProjectsDoesNotHaveRepositories() {
        val m = Mockery()
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                oneOf(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                oneOf(repositoryManager).getRepositories(project, true)
                will(returnValue(emptyList<Repository>()))

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(emptyList<SBuildFeatureDescriptor>()))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty())
        m.assertIsSatisfied()
    }

    fun getEmptyListIfProjectsDoesNotHaveNuGetFeeds() {
        val m = object : Mockery() {
            init {
                setImposteriser(ClassImposteriser.INSTANCE)
            }
        }
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val repository = m.mock(Repository::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                oneOf(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                oneOf(repositoryManager).getRepositories(project, true)
                will(returnValue(listOf(repository)))

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(emptyList<SBuildFeatureDescriptor>()))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty())
        m.assertIsSatisfied()
    }

    fun getEmptyListIfProjectFeedDoesNotHaveIndexing() {
        val m = object : Mockery() {
            init {
                setImposteriser(ClassImposteriser.INSTANCE)
            }
        }
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val repositoryType = m.mock(RepositoryType::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                one(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                allowing(project).projectId
                will(returnValue("_Root"))

                oneOf(repositoryManager).getRepositories(project, true)
                will(object: Action {
                    override fun describeTo(description: Description?) = Unit

                    override fun invoke(invocation: Invocation?): Any {
                        return listOf(NuGetRepository(repositoryType, project, mapOf("name" to "default")))
                    }
                })

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(emptyList<SBuildFeatureDescriptor>()))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertTrue(feeds.isEmpty())
        m.assertIsSatisfied()
    }

    fun getFeedsFromProjectWithIndexing() {
        val m = object : Mockery() {
            init {
                setImposteriser(ClassImposteriser.INSTANCE)
            }
        }
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val repositoryType = m.mock(RepositoryType::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                one(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                allowing(project).projectId
                will(returnValue("_Root"))

                oneOf(repositoryManager).getRepositories(project, true)
                will(object: Action {
                    override fun describeTo(description: Description?) = Unit

                    override fun invoke(invocation: Invocation?): Any {
                        return listOf(NuGetRepository(repositoryType, project, mapOf(
                            "name" to "default",
                            "indexPackages" to "true"
                        )))
                    }
                })

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(emptyList<SBuildFeatureDescriptor>()))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertEquals(feeds.size, 1)
        Assert.assertEquals(feeds.first(), NuGetFeedData.DEFAULT)
        m.assertIsSatisfied()
    }

    fun getFeedsFromNuGetIndexerFeature() {
        val m = Mockery()
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val feature = m.mock(SBuildFeatureDescriptor::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                one(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                allowing(project).projectId
                will(returnValue("_Root"))

                oneOf(repositoryManager).getRepositories(project, true)
                will(returnValue(emptyList<Repository>()))

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(listOf(feature)))

                oneOf(feature).parameters
                will(returnValue(mapOf("feed" to "_Root/default")))

                oneOf(projectManager).findProjectByExternalId("_Root")
                will(returnValue(project))

                oneOf(repositoryManager).hasRepository(project, "nuget", "default")
                will(returnValue(true))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertEquals(feeds.size, 1)
        Assert.assertEquals(feeds.first(), NuGetFeedData.DEFAULT)
        m.assertIsSatisfied()
    }

    fun getDistinctFeedsFromNuGetIndexerFeature() {
        val m = Mockery()
        val projectManager = m.mock(ProjectManager::class.java)
        val repositoryManager = m.mock(RepositoryManager::class.java)
        val build = m.mock(SBuild::class.java)
        val project = m.mock(SProject::class.java)
        val feature = m.mock(SBuildFeatureDescriptor::class.java)
        val feedsProvider = NuGetBuildFeedsProviderImpl(projectManager, repositoryManager)

        m.checking(object : Expectations() {
            init {
                one(build).projectId
                will(returnValue("_Root"))

                oneOf(projectManager).findProjectById("_Root")
                will(returnValue(project))

                allowing(project).projectId
                will(returnValue("_Root"))

                oneOf(repositoryManager).getRepositories(project, true)
                will(returnValue(emptyList<Repository>()))

                oneOf(build).getBuildFeaturesOfType("NuGetPackagesIndexer")
                will(returnValue(listOf(feature, feature)))

                allowing(feature).parameters
                will(returnValue(mapOf("feed" to "_Root/default")))

                allowing(projectManager).findProjectByExternalId("_Root")
                will(returnValue(project))

                allowing(repositoryManager).hasRepository(project, "nuget", "default")
                will(returnValue(true))
            }
        })

        val feeds = feedsProvider.getFeeds(build)

        Assert.assertEquals(feeds.size, 1)
        Assert.assertEquals(feeds.first(), NuGetFeedData.DEFAULT)
        m.assertIsSatisfied()
    }
}
