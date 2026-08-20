# Lazy Experimentation for Kotlin / Android

Lazy Experimentation gives Android apps local experiment assignment, feature delivery, and outcome capture through Lazy's control plane.

```kotlin
repositories { maven(url = "https://jitpack.io") }
dependencies {
    implementation("com.github.aboul3ata:lazy-experimentation-kotlin:0.1.1")
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
