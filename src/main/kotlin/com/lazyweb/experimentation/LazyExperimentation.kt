package com.lazyweb.experimentation

import com.sdk.growthbook.GBSDKBuilder
import com.sdk.growthbook.GrowthBookSDK
import com.sdk.growthbook.model.GBString
import com.sdk.growthbook.model.GBValue
import com.sdk.growthbook.network.GBNetworkDispatcherKtor
import com.sdk.growthbook.network.NetworkDispatcher

const val LAZY_EXPERIMENTATION_API_HOST = "https://experimentation.lazyweb.com"

class LazyExperimentation private constructor(
    val growthBook: GrowthBookSDK,
    private val sender: EventSender,
) {
    fun capture(eventName: String, properties: Map<String, Any> = emptyMap(), value: Number? = null) {
        require(EVENT_NAME.matches(eventName)) { "eventName must be a lowercase Lazyweb key" }
        require(value?.toDouble()?.isFinite() != false) { "value must be finite" }
        sender.capture(eventName, properties, value)
    }

    fun close() {
        growthBook.close()
    }

    companion object {
        fun create(
            clientKey: String,
            subjectId: String,
            attributes: Map<String, GBValue> = emptyMap(),
            apiHost: String = LAZY_EXPERIMENTATION_API_HOST,
            networkDispatcher: NetworkDispatcher = GBNetworkDispatcherKtor(),
        ): LazyExperimentation {
            require(clientKey.isNotBlank()) { "clientKey is required" }
            require(subjectId.isNotBlank() && subjectId.length <= 256 && '@' !in subjectId) {
                "subjectId must be an opaque identifier"
            }
            val normalizedHost = apiHost.trimEnd('/')
            require(normalizedHost.startsWith("https://") || normalizedHost.startsWith("http://localhost")) {
                "apiHost must be HTTPS"
            }
            val userAttributes = attributes + ("id" to GBString(subjectId))
            val sender = EventSender(networkDispatcher, normalizedHost, clientKey, subjectId)
            val growthBook = GBSDKBuilder(
                apiKey = clientKey,
                apiHost = normalizedHost,
                networkDispatcher = networkDispatcher,
                attributes = userAttributes,
                trackingCallback = { experiment, result ->
                    sender.exposure(experiment.key, result.variationId)
                },
            ).initialize()
            return LazyExperimentation(growthBook, sender)
        }
    }
}

private class EventSender(
    private val dispatcher: NetworkDispatcher,
    private val apiHost: String,
    private val clientKey: String,
    private val subjectId: String,
) {
    fun exposure(experimentKey: String, variationId: Int) {
        send(
            "Experiment Viewed",
            mapOf("experimentId" to experimentKey, "variationId" to variationId),
        )
    }

    fun capture(eventName: String, properties: Map<String, Any>, value: Number?) {
        send(eventName, if (value == null) properties else properties + ("value" to value))
    }

    private fun send(eventName: String, properties: Map<String, Any>) {
        dispatcher.consumePOSTRequest(
            url = "$apiHost/track?client_key=$clientKey",
            bodyParams = mapOf(
                "event_name" to eventName,
                "properties" to properties,
                "attributes" to mapOf("id" to subjectId),
                "device_id" to subjectId,
            ),
            onSuccess = {},
            onError = {},
        )
    }
}

private val EVENT_NAME = Regex("^[a-z0-9][a-z0-9._-]{0,127}$")
