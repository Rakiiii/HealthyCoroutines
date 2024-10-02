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

package dev.rakiiii.healthycoroutines.operations

import dev.rakiiii.healthycoroutines.api.CatchingLogger
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCatchingLogger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Exception handling helper for doing some extra work on error and then exit the parent function
 *
 * Example,
 *
 * ```
 * class SomeViewModel(private val getSomething: GetSomethingUseCase) {
 *     fun someIoOperation(id: String) = onIo {
 *              ...
 *
 *          val something = returnOrExit(getSomething::invoke) {
 *             sendEvent(ErrorMessageEvent(...))
 *             return@onIo
 *          }
 *
 *              ...
 *     }
 *
 *     class GetSomethingUseCase() {
 *            override suspend fun invoke(): String {
 *                  ...
 *            }
 *     }
 * }
 * ```
 */
suspend inline fun <T> returnOrExit(
    action: suspend () -> T,
    exit: suspend (Throwable) -> Nothing,
): T = returnOrExit(rethrow(), DefaultCatchingLogger, action, exit)

/**
 * Exception handling helper for doing some extra work on error and then exit the parent function
 *
 * @param onCancellation [CancellationHandler] handler for [CancellationException]
 * @param logger [CatchingLogger] logger for exception
 * @param action Operation
 * @param exit Exception handler callback which must end parent function
 */
suspend inline fun <T> returnOrExit(
    onCancellation: CancellationHandler<T>,
    logger: CatchingLogger,
    action: suspend () -> T,
    exit: suspend (Throwable) -> Nothing,
): T {
    return try {
        action()
    } catch (e: CancellationException) {
        onCancellation(e)
    } catch (e: Throwable) {
        logger(e)
        exit(e)
    }
}

/**
 * Exception handling helper for ignoring errors
 *
 * Rethrows [CancellationException]
 *
 * @param logger [CatchingLogger] for exception, by default is DefaultCatchingLogger
 * @param action Operation
 */
suspend fun ignoreError(
    logger: CatchingLogger = DefaultCatchingLogger,
    action: suspend () -> Unit,
) {
    return returnOrDefault(logger, Unit, action)
}

/**
 * Exception handling helper for getting null on error
 *
 * Rethrows [CancellationException]
 * Uses [DefaultCatchingLogger] for exceptions logging
 *
 * @param action Operation
 */
suspend fun <T> getOrNull(action: suspend () -> T): T? {
    return returnOrDefault(DefaultCatchingLogger, null, action)
}

/**
 * Exception handling helper for getting default value on error
 *
 * Rethrows [CancellationException]
 * Uses [DefaultCatchingLogger] for exceptions logging
 *
 * @param default Default value
 * @param action Operation
 */
suspend inline fun <T> returnOrDefault(
    default: T,
    action: suspend () -> T,
): T {
    return returnOrDefault(DefaultCatchingLogger, default, action)
}

/**
 * Exception handling helper for getting default value on error
 *
 * Rethrows [CancellationException]
 * Uses [DefaultCatchingLogger] for exceptions logging
 *
 * @param defaultProducer Default value producer
 * @param action Operation
 */
suspend inline fun <T> returnOrDefault(
    defaultProducer: suspend () -> T,
    action: suspend () -> T,
): T {
    return returnOrDefault(rethrow(), DefaultCatchingLogger, defaultProducer, action)
}

/**
 * Exception handling helper for getting default value on error
 *
 * Rethrows [CancellationException]
 *
 * @param logger [CatchingLogger] logger for exception
 * @param default Default value
 * @param action Operation
 */
suspend inline fun <T> returnOrDefault(
    logger: CatchingLogger,
    default: T,
    action: suspend () -> T,
): T {
    return returnOrDefault(rethrow(), logger, { default }, action)
}

/**
 * Exception handling helper for getting default value on error
 *
 * @param onCancellation [CancellationHandler] handler for [CancellationException]
 * @param logger [CatchingLogger] logger for exception
 * @param defaultProducer Default value producer
 * @param action Operation
 */
suspend inline fun <T> returnOrDefault(
    onCancellation: CancellationHandler<T>,
    logger: CatchingLogger,
    defaultProducer: suspend () -> T,
    action: suspend () -> T,
): T {
    return runCatching { action() }
        .getOrElse {
            if (it is CancellationException) {
                onCancellation(it)
            } else {
                logger(it)
                defaultProducer()
            }
        }
}
