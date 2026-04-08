## 2026-04-08: Unreleased 🚑 stop hammering GitHub cache after HTTP 429
* Treat GitHub Actions cache rate limits as a temporary cache miss
* Honor the `Retry-After` cooldown when skipping further cache requests in the same job

## 2024-05-03: v3 🚀 Move to actions/cache@v4 API, resolve "Cache service responded with 422"
* Bump to actions/cache@v4 API

* Bump Gradle to 8.14
* Bump to Kotlin 2.1.20
* Bump kotlinx-coroutines to 1.10.2
* Bump kotlin-serialization to 1.8.1
* Bump kotlin-wrappers to 2025.5.2

## 2024-07-25: v2, v1.21 🚀 Move to node20

* Bump `node16` to `node20`. This resolves "node16 is deprecated" warning.

* Bump Gradle to 8.8
* Bump kotlinx-coroutines to 1.8.1
* Bump kotlin-serialization to 1.7.1
* Bump wrapper-validation-action to v3
* Bump setup-java, checkout to v4

## 2023-02-18: v1.21 🚑 bump dependencies

* Add wrapper-validation-action
* Bump checkout and setup-java to v3

## 2023-02-18: v1.20 🚑 Avoid crash on missing layer-..json file

* fix: avoid failure when index restore misses layer-..json file
* Migrate to kotlin-wrappers:kotlin-actions-toolkit for better Kotlin wrappers https://github.com/burrunan/gradle-cache-action/pull/65
* Bump Gradle to 8.0.1
* Bump Kotlin to 1.8.10

## 2023-02-03: v1.19 🚑 Support nested version catalogs

* Also consider nested version catalogs in default dependency paths: https://github.com/burrunan/gradle-cache-action/issues/63

Thanks to [Vampire](https://github.com/Vampire) for the contribution.

## 2023-02-03: v1.18 🚑 fix crash when git log returns a string with a newline

* Trim the resulting SHA to prevent failures like in https://github.com/burrunan/gradle-cache-action/issues/63

## 2023-02-03: v1.17 🚀 better types for list arguments in github-workflows-kt

* Mark list arguments better: https://github.com/burrunan/gradle-cache-action/pull/61
* Add gradle/libs.versions.toml to the default dependency paths: https://github.com/burrunan/gradle-cache-action/pull/62

Thanks to [Vampire](https://github.com/Vampire) for the contribution.

## 2023-01-23: v1.16 🚀 added types for github-workflows-kt

See https://github.com/burrunan/gradle-cache-action/issues/58

Thanks to [Vampire](https://github.com/Vampire) for the contribution.

## 2022-11-27: v1.15 ⬆️ bump dependencies
Includes all the fixes from 1.13 and 1.14.

## 2022-10-29: v1.14 ⬆️ bump dependencies
*Unreleased*: the code was not compatible with `kotlin-wrappers/node`, so it did not work. Use 1.15 instead.

* bump @actions/core: 1.9.1 -> 1.10.0 (fix set-state warning)
* bump @actions/cache: 3.0.4 -> 3.0.6

## 2022-08-24: v1.13 ⬆️ bump dependencies
*Unreleased*: the code was not compatible with `kotlin-wrappers/node`, so it did not work.  Use 1.15 instead.

* bump @actions/core: 1.9.0 -> 1.9.1
* bump @actions/cache: 3.0.0 -> 3.0.4
* Move from kotlinx-node to kotlin-wrappers/node
* Print stacktrace on cache proxy server failure
* Bump Gradle to 7.5.1

## 2022-07-15: v1.12 ⬆️ bump dependencies

* Kotlin 1.4.31 legacy -> 1.7.10 IR
* @actions/cache: 1.0.1 -> 3.0.0