/*
 * Copyright 2024-today Evgeniy S. Sudarskiy 
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

package dev.rakiiii.healthycoroutines.handlers

import dev.rakiiii.healthycoroutines.api.BaseCoroutineExceptionHandler
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCoroutineLogger
import dev.rakiiii.healthycoroutines.api.CoroutineLogger
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine exception handler which logs error before handling it, handling exception with type ignore
 *
 * @param logger Logger for error
 * @param internalHandler Actual error handler
 */
fun onAnyErrorWithLog(logger: CoroutineLogger = DefaultCoroutineLogger, internalHandler: () -> Unit): BaseCoroutineExceptionHandler =
    LogCoroutineExceptionHandler(logger = logger) { internalHandler() }

/**
 * Coroutine exception handler which logs error before handling it
 *
 * @param logger Logger for error
 * @param payload Actual error handler
 */
fun onErrorWithLog(
    logger: CoroutineLogger = DefaultCoroutineLogger,
    payload: LogCoroutineExceptionHandler.ExceptionPayload,
): BaseCoroutineExceptionHandler = LogCoroutineExceptionHandler(logger = logger, payload = payload)

/** Coroutines exception handler which rethrow exception */
fun rethrowError(): BaseCoroutineExceptionHandler =
    LogCoroutineExceptionHandler(logger = CoroutineLogger.NO_LOGGER, payload = LogCoroutineExceptionHandler.RETHROW_PAYLOAD)

/**
 * Coroutines exception handler which log exception and then rethrow it
 *
 * @param logger [CoroutineLogger] logger for exception, by default is [DefaultCoroutineLogger]
 */
fun logAndRethrowError(logger: CoroutineLogger = DefaultCoroutineLogger): BaseCoroutineExceptionHandler =
    LogCoroutineExceptionHandler(logger = logger, payload = LogCoroutineExceptionHandler.RETHROW_PAYLOAD)

/**
 * Coroutines exception handler which only log exception
 *
 * @param logger [CoroutineLogger] logger for exception, by default is [DefaultCoroutineLogger]
 */
fun logError(logger: CoroutineLogger = DefaultCoroutineLogger): BaseCoroutineExceptionHandler =
    LogCoroutineExceptionHandler(logger = logger, payload = LogCoroutineExceptionHandler.NO_PAYLOAD)

/**
 * Coroutine exception handler which logs error before handling it
 *
 * Ignores coroutine context on handling
 *
 * @property logger Logger for error
 * @property payload Actual error handler
 */
open class LogCoroutineExceptionHandler(
    protected val logger: CoroutineLogger,
    protected val payload: ExceptionPayload,
) : BaseCoroutineExceptionHandler() {
    companion object {
        val NO_PAYLOAD: ExceptionPayload = ExceptionPayload {}
        val RETHROW_PAYLOAD: ExceptionPayload = ExceptionPayload { throw it }
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger(context, exception)
        payload(exception)
    }

    fun interface ExceptionPayload {
        operator fun invoke(t: Throwable)
    }
}