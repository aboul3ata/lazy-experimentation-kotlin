package com.lazyweb.experimentation

import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LazyExperimentationDeviceTest {
    @Test
    fun packageInitializesAndCapturesOnAndroid() {
        val dispatcher = DeviceFakeDispatcher()
        val experiments = LazyExperimentation.create(
            clientKey = "lwe_cfg_device_test",
            subjectId = "emulator-device",
            apiHost = "https://example.test",
            networkDispatcher = dispatcher,
        )
        experiments.capture("device_test_completed")
        assertEquals("device_test_completed", dispatcher.lastEvent)
        experiments.close()
    }
}

private class DeviceFakeDispatcher : NetworkDispatcher {
    var lastEvent: String? = null

    override fun consumeGETRequest(
        request: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit,
    ): Job = CoroutineScope(Dispatchers.Unconfined).launch { onSuccess("{\"features\":{}}") }

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
        lastEvent = bodyParams["event_name"] as? String
        onSuccess("{\"accepted\":1}")
    }
}
