# Growbolt Android SDK

[![](https://jitpack.io/v/Nadheria/GrowboltSdk-android.svg)](https://jitpack.io/#Nadheria/GrowboltSdk-android)

Native Android Offerwall SDK for the Growbolt platform.

[![](https://jitpack.io/v/growbolt/growbolt-android-sdk.svg)](https://jitpack.io/#growbolt/growbolt-android-sdk)

---

## Requirements
- Android 7.0+ (API 24+)
- Kotlin 1.9+

---

## Installation

### JitPack (recommended for development)

**Step 1.** Add JitPack to your root `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2.** Add the dependency:
```kotlin
dependencies {
    implementation("com.github.growbolt:growbolt-android-sdk:1.0.0")
}
```

### Maven Central (production)
```kotlin
dependencies {
    implementation("com.growbolt:growbolt-sdk:1.0.0")
}
```

---

## Integration

### Step 1 — Your backend issues the SDK token

Your backend must call:
```
POST /api/v1/sdk/token/
Authorization: Bearer <dashboard_jwt>
{
  "device_id": "...",
  "user_agent": "..."
}
```
Response:
```json
{ "key": "opaque-sdk-token", "token_type": "SdkToken" }
```
Return that `key` to your app.

### Step 2 — Initialise the SDK (once, in Application.onCreate or first Activity)

```kotlin
GrowboltSdk.init(
    context = applicationContext,
    config = GrowboltConfig(
        sdkToken = fetchedTokenFromYourBackend,
        userId   = "stable-user-id",         // used as sub4 for Ongoing lookups
        baseUrl  = "https://api.growbolt.com",
        currencySymbol = "₹",
        debug    = BuildConfig.DEBUG
    )
)
```

### Step 3 — Register callback

```kotlin
GrowboltSdk.registerOfferwallCallback(object : OfferwallCallback {
    override fun onOfferwallLoadingStarted() {}
    override fun onOfferwallLoadingFinished() {}
    override fun onOfferwallLoadingFailed(error: String) {}
    override fun onOfferwallRewardReceived(amount: Double, currency: String) {
        // Credit the user in your system
    }
    override fun onOfferwallClosed() {}
})
```

### Step 4 — Show the offerwall

```kotlin
GrowboltSdk.showOfferwall(activity)
```

### Step 5 — Unregister in onDestroy

```kotlin
override fun onDestroy() {
    super.onDestroy()
    GrowboltSdk.unregisterOfferwallCallback()
}
```

---

## API Reference

| Method | Description |
|---|---|
| `GrowboltSdk.init(context, config)` | Initialise SDK. Call once. |
| `GrowboltSdk.showOfferwall(context)` | Launch offerwall Activity. |
| `GrowboltSdk.registerOfferwallCallback(cb)` | Register event callback. |
| `GrowboltSdk.unregisterOfferwallCallback()` | Clear callback (call in onDestroy). |
| `GrowboltSdk.updateToken(newToken)` | Update token if backend rotates it. |
| `GrowboltSdk.isInitialised()` | Check if SDK is ready. |

---

## Screens

| Screen | Description |
|---|---|
| Offerwall | Banner carousel + category chips + offer list |
| Offer Detail | Full offer info + CTA button with sub4 injected |
| Offer Status | Pending / Completed / Failed tabs with counts |
| Empty State | "Explore Offer" with CTA back to offerwall |

---

## Publishing

### JitPack
Push a git tag (`v1.0.0`) — JitPack builds automatically.

### Maven Central
Set secrets in GitHub repo settings:
- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY`, `SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`

Then create a GitHub Release — CI publishes automatically.

---

## License
MIT
