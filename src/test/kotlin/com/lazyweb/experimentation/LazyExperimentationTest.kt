package com.lazyweb.experimentation

import com.sdk.growthbook.network.NetworkDispatcher
import com.sdk.growthbook.utils.Resource
import com.sdk.growthbook.utils.SSEConnectionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LazyExperimentationTest {
    @Test
    fun `GrowthBook loads features and Lazy sends outcomes through its dispatcher`() {
        val dispatcher = FakeDispatcher()
        val experiments = LazyExperimentation.create(
            clientKey = "lwe_cfg_test",
            subjectId = "android-device-123",
            apiHost = "https://example.test",
            networkDispatcher = dispatcher,
        )

        experiments.growthBook.refreshCache()
        assertTrue(dispatcher.fetched.await(2, TimeUnit.SECONDS))
        assertTrue(dispatcher.gets.all { it == "https://example.test/api/features/lwe_cfg_test" })
        experiments.capture("onboarding_completed", mapOf("steps" to 3))
        assertEquals("https://example.test/track?client_key=lwe_cfg_test", dispatcher.posts.single().first)
        assertEquals("onboarding_completed", dispatcher.posts.single().second["event_name"])
        assertTrue(experiments.growthBook.getFeatures().isEmpty())
        experiments.close()
    }
}

private class FakeDispatcher : NetworkDispatcher {
    val gets = mutableListOf<String>()
    val posts = mutableListOf<Pair<String, Map<String, Any>>>()
    val fetched = CountDownLatch(1)

    override fun consumeGETRequest(
        request: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit,
    ): Job = CoroutineScope(Dispatchers.Unconfined).launch {
        gets += request
        onSuccess("{\"features\":{}}")
        fetched.countDown()
    }

    override fun consumeSSEConnection(
        url: String,
        sseController: SSEConnectionController?,
    ): Flow<Resource<String>> = emptyFlow()

    override fun consumePOSTRequest(
        url: String,
        bodyParams: Map<String, Any>,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        posts += url to bodyParams
        onSuccess("{\"accepted\":1}")
    }
}
