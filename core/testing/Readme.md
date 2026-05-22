# Shared Library Test For Any Module

`:core:testing/src/main` (which acts as a library for other modules' tests) so that any module in your app can use these fake feature flags in their tests.

`@TestInstallIn` is used to swap out real implementations with fakes for integration tests (tests that use Hilt). It belongs in the `androidTest` source set (or a shared testing library like `:core:testing`).

## Usage in Tests

Simply annotate your test class with `@HiltAndroidTest`. Hilt will automatically pick up `FakeFeatureFlagsModule` and inject `DefaultFeatureFlags` instead of the real `AppFeatureFlags`.

```kotlin
@HiltAndroidTest
class MyFeatureTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    // ... tests will now have all features enabled by default
}
```

# When writing an instrumented test (in `androidTest`) or a Robolectric test (in `test`) that uses Hilt:

- Annotate your test with `@HiltAndroidTest`
- Add the `HiltAndroidRule`
- Hilt will automatically swap `AppFeatureFlags` for `DefaultFeatureFlags`

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MyNavigationTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    
    @Test
    fun testEverythingEnabled() {
        // featureFlags will be DefaultFeatureFlags here
    }
}
```