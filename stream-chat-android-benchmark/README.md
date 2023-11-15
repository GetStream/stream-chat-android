
## Baseline Profiles

[Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview) empower you to achieve faster code execution, boasting potential speed improvements of around 20-30% from the first launch. This is accomplished by delivering pre-compiled source code information, effectively bypassing interpretation and [just-in-time (JIT)](https://source.android.com/docs/core/runtime/jit-compiler#flow) compilation steps when users launch your application.

When you integrate Baseline Profiles into your application, [Android Runtime (ART)](https://source.android.com/docs/core/runtime) optimizes precise code pathways by utilizing the supplied source code profiles. These profiles consist of information regarding your classes and methods employed in [Ahead-of-Time (AOT)](https://source.android.com/docs/core/runtime#AOT_compilation
) compilation.

But it's tricky to generate baseline profiles depending on the benchmark and Baseline Profile plugin versions.

For the latest commit with Benchmark 1.2.0 stable, you should disable or remove some codes before running baseline profiles.

1. Disabled R8 full mode on the `gradle.properties`:

```
android.enableR8.fullMode=false
```

2. Delete the `StartupBenchmark.kt` file.

3. Run the commandline below:

```
./gradlew generateBaselineProfile
```