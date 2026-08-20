# Lazy Experimentation for Kotlin / Android

This Android library is a thin initializer around GrowthBook's official Kotlin SDK. GrowthBook owns feature fetching, caching, targeting, hashing, assignment, and exposure callbacks.

```kotlin
repositories { maven(url = "https://jitpack.io") }
dependencies {
    implementation("com.github.aboul3ata:lazy-experimentation-kotlin:0.1.0")
}
```

```kotlin
val experiments = LazyExperimentation.create(
    clientKey = "lwe_cfg_...",
    subjectId = deviceId,
    attributes = mapOf("plan" to GBString("pro")),
)

val enabled = experiments.growthBook.featureValue<Boolean>("new-onboarding") ?: false
experiments.capture("onboarding_completed", mapOf("steps" to 3))
```

Official engine: [`growthbook-kotlin`](https://github.com/growthbook/growthbook-kotlin).
