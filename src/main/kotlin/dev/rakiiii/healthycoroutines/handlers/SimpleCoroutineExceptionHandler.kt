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
import dev.rakiiii.healthycoroutines.api.defaults.DefaultThrowableFilter
import dev.rakiiii.healthycoroutines.api.ThrowableFilter
import dev.rakiiii.healthycoroutines.handlers.SimpleErrorHandler.ErrorHandler
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine exception handler which ignores context of the error and type of the error
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Exception handler
 */
fun onAnyError(internalHandler: () -> Unit): BaseCoroutineExceptionHandler =
    SimpleErrorHandler({ internalHandler() }, DefaultThrowableFilter)

/**
 * Coroutine exception handler which ignores context of the error
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property handler Exception handler
 */
fun onError(handler: ErrorHandler): BaseCoroutineExceptionHandler =
    SimpleErrorHandler(handler, DefaultThrowableFilter)

/**
 * Coroutine exception handler which ignores context of the error with integrated [ThrowableFilter] use
 *
 * @property internalHandler Exception handler
 * @property throwableFilter [ThrowableFilter]
 */
open class SimpleErrorHandler(
    protected val internalHandler: ErrorHandler,
    protected val throwableFilter: ThrowableFilter,
) : BaseCoroutineExceptionHandler() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (throwableFilter(exception)) return
        internalHandler(exception)
    }

    fun interface ErrorHandler {
        operator fun invoke(exception: Throwable)
    }
}