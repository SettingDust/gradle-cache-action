/*
 * Copyright 2020 Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actions.cache

import actions.core.LogLevel
import actions.core.info
import actions.core.log
import actions.core.warning
import kotlin.js.Date

private object GitHubCacheThrottle {
    private const val DEFAULT_RETRY_AFTER_MILLIS = 60_000.0

    private var disabledUntil: Double = 0.0
    private var reason: String = ""

    fun remainingMillis(now: Double = Date.now()): Double = (disabledUntil - now).coerceAtLeast(0.0)

    fun isEnabled(): Boolean = remainingMillis() <= 0

    fun disable(t: Throwable, operation: String, key: String) {
        val retryAfterMillis = t.retryAfterMillis() ?: DEFAULT_RETRY_AFTER_MILLIS
        val until = Date.now() + retryAfterMillis
        if (until > disabledUntil) {
            disabledUntil = until
            reason = t.message ?: "HTTP 429"
        }
        warning(
            "GitHub Actions cache is being rate limited while $operation $key. " +
                "Skipping cache traffic for ${(retryAfterMillis / 1000).toInt()} seconds. " +
                "Reason: ${t.message ?: "HTTP 429"}",
        )
    }

    fun logSkip(operation: String, key: String, logLevel: LogLevel) {
        log(logLevel) {
            "Skipping GitHub Actions cache $operation for $key for ${(remainingMillis() / 1000).toInt()} more seconds. $reason"
        }
    }
}

private fun Any?.toDoubleOrNull(): Double? = when (this) {
    null -> null
    is Number -> toDouble()
    is String -> trim().toDoubleOrNull()
    else -> toString().trim().toDoubleOrNull()
}

private fun Throwable.isGitHubCacheRateLimit(): Boolean {
    val dynamic = asDynamic()
    val statusCode = dynamic.statusCode.toDoubleOrNull()?.toInt()
        ?: dynamic.status.toDoubleOrNull()?.toInt()
        ?: dynamic.response?.statusCode.toDoubleOrNull()?.toInt()
    return statusCode == 429 || message?.contains("429") == true
}

private fun Throwable.retryAfterMillis(): Double? {
    val dynamic = asDynamic()
    return dynamic.retryAfterMillis.toDoubleOrNull()
        ?: dynamic.retryAfter.toDoubleOrNull()?.times(1000)
        ?: dynamic.response?.headers?.get?.call(dynamic.response.headers, "retry-after").toDoubleOrNull()?.times(1000)
        ?: dynamic.response?.headers?.get?.call(dynamic.response.headers, "Retry-After").toDoubleOrNull()?.times(1000)
        ?: dynamic.response?.headers?.get("retry-after").toDoubleOrNull()?.times(1000)
        ?: dynamic.response?.headers?.get("Retry-After").toDoubleOrNull()?.times(1000)
        ?: dynamic.response?.headers?.asDynamic()?.["retry-after"].toDoubleOrNull()?.times(1000)
        ?: dynamic.headers?.get?.call(dynamic.headers, "retry-after").toDoubleOrNull()?.times(1000)
        ?: dynamic.headers?.get("retry-after").toDoubleOrNull()?.times(1000)
        ?: dynamic.headers?.asDynamic()?.["retry-after"].toDoubleOrNull()?.times(1000)
}

suspend fun restoreAndLog(
    paths: List<String>, primaryKey: String,
    restoreKeys: List<String> = listOf(),
    version: String,
    logLevel: LogLevel = LogLevel.INFO,
): RestoreType {
    if (!GitHubCacheThrottle.isEnabled()) {
        GitHubCacheThrottle.logSkip("restore", primaryKey, logLevel)
        return RestoreType.None
    }
    val result = try {
        when {
            restoreKeys.isEmpty() -> restoreCache(paths.toTypedArray(), version + primaryKey)
            else -> restoreCache(
                paths.toTypedArray(),
                version + primaryKey,
                restoreKeys.map { version + it }.toTypedArray(),
            )
        }
    } catch (t: Throwable) {
        when (t.asDynamic().name) {
            "ValidationError" -> throw t
            else -> {
                if (t.isGitHubCacheRateLimit()) {
                    GitHubCacheThrottle.disable(t, "restoring", primaryKey)
                    return RestoreType.None
                }
                warning("Error while loading $primaryKey: ${t.message}")
                return RestoreType.None
            }
        }
    }
    result?.removePrefix(version)?.let {
        return if (it.endsWith(primaryKey)) RestoreType.Exact(it) else RestoreType.Partial(it)
    }
    log(logLevel) { "Cache was not found for version=$version, primaryKey=$primaryKey, restore keys=${restoreKeys.joinToString(", ")}" }
    return RestoreType.None
}

suspend fun saveAndLog(
    paths: List<String>,
    key: String,
    version: String,
    logLevel: LogLevel = LogLevel.INFO,
) {
    if (!GitHubCacheThrottle.isEnabled()) {
        GitHubCacheThrottle.logSkip("upload", key, logLevel)
        return
    }
    try {
        saveCache(paths.toTypedArray(), version + key)
    } catch (t: Throwable) {
        // TODO: propagate error
        when (t.asDynamic().name) {
            "ValidationError" -> throw t
            "ReserveCacheError" -> info(t.message ?: "Unknown ReserveCacheError")
            else -> when {
                t.isGitHubCacheRateLimit() ->
                    GitHubCacheThrottle.disable(t, "uploading", key)
                t.message?.contains("Cache already exists") == true ->
                    log(logLevel) { "Error while uploading $key: ${t.message}" }
                else ->
                    warning("Error while uploading $key: ${t.message}")
            }
        }
    }
}