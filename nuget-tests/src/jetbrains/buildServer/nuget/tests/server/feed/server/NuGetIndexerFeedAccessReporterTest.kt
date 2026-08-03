package jetbrains.buildServer.nuget.tests.server.feed.server

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.security.IndexerFeedsResolutionResult
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProvider
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetIndexerFeedAccessReporter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.util.EventDispatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.lib.legacy.ClassImposteriser
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
class NuGetIndexerFeedAccessReporterTest {

    private lateinit var m: Mockery
    private lateinit var feedsProvider: NuGetBuildFeedsProvider
    private lateinit var serverSettings: NuGetServerSettings
    private lateinit var dispatcher: EventDispatcher<BuildServerListener>
    private lateinit var build: SRunningBuild
    private lateinit var reporter: NuGetIndexerFeedAccessReporter

    @Suppress("UNCHECKED_CAST")
    @BeforeMethod
    fun setUp() {
        m = object : Mockery() { init { setImposteriser(ClassImposteriser.INSTANCE) } }
        feedsProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        serverSettings = m.mock(NuGetServerSettings::class.java)
        dispatcher = m.mock(EventDispatcher::class.java) as EventDispatcher<BuildServerListener>
        build = m.mock(SRunningBuild::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(dispatcher).addListener(with(any(BuildServerListener::class.java)))
                allowing(serverSettings).isNuGetServerEnabled; will(returnValue(false))
            }
        })
        reporter = NuGetIndexerFeedAccessReporter(feedsProvider, serverSettings, dispatcher)
    }

    fun reportsBuildProblemForRejectedFeed() {
        m.checking(object : Expectations() {
            init {
                allowing(feedsProvider).resolveIndexerFeeds(build)
                will(returnValue(IndexerFeedsResolutionResult(emptySet(), listOf("Foreign/packages"))))
                oneOf(build).addBuildProblem(with(any(BuildProblemData::class.java)))
            }
        })

        reporter.buildStarted(build)
        m.assertIsSatisfied()
    }

    fun doesNotReportWhenNoRejectedFeeds() {
        m.checking(object : Expectations() {
            init {
                allowing(feedsProvider).resolveIndexerFeeds(build)
                will(returnValue(IndexerFeedsResolutionResult(setOf(NuGetFeedData("Child", "packages")), emptyList())))
                never(build).addBuildProblem(with(any(BuildProblemData::class.java)))
            }
        })

        reporter.buildStarted(build)
        m.assertIsSatisfied()
    }
}
