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

### Step 2 — Initialise the SDK (once, in Application.onCreate)

Call this once, as early as possible — it's safe to call before the user has logged in.
`userId` is optional at this point; pass it later via `identify()` once you know who the user is.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        GrowboltSdk.init(
            context = this,
            config = GrowboltConfig(
                sdkToken = "fetchedTokenFromYourBackend",  // Fetch from your backend
                debug    = BuildConfig.DEBUG
            )
        )

        // If the user is already logged in from a previous session, identify them now
        val savedUserId = yourSharedPrefs.getString("user_id", null)
        if (!savedUserId.isNullOrBlank()) {
            GrowboltSdk.identify(savedUserId)
        }
    }
}
```

### Step 3 — Identify the user after login

As soon as your own login flow has a stable identifier for the user (phone number, email,
internal user id — whatever you use), call:

```kotlin
GrowboltSdk.identify(userId = phoneNumberOrEmailOrUserId)
```

This can be called any time after `init()`, from any screen — not just at app start.
Safe to call again later if the identifier changes (e.g. account merge).

### Step 4 — Reset on logout

```kotlin
GrowboltSdk.reset()
```

Clears the stored `userId` without tearing down the rest of the SDK — no need to call
`init()` again for the next user to log in, just call `identify()` once they do.

### Step 5 — Register callback

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

### Step 6 — Show the offerwall

```kotlin
GrowboltSdk.showOfferwall(activity)
```

### Step 7 — Unregister in onDestroy

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
| `GrowboltSdk.init(context, config)` | Initialise SDK. Call once, safe before login (userId optional). |
| `GrowboltSdk.identify(userId)` | Attach/update the user identifier after login. Call any time after `init()`. |
| `GrowboltSdk.reset()` | Clear the current userId on logout. SDK stays initialised. |
| `GrowboltSdk.setDebugEnabled(enabled)` | Toggle verbose logcat output at runtime, no re-init needed. |
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

## License
MIT
