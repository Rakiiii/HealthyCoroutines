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

package dev.rakiiii.healthycoroutines.api

import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Base class for abstraction over coroutine, that represents work with coroutine as with some container
 * The main reason of existing is to make work with [Job] forbidden
 */
abstract class BaseCoroutineContainer {
    /** Returns true when this coroutine is active
     * -- it was already started and has not completed nor was
     * cancelled yet. The coroutine that is waiting for its children to complete is still considered to be active
     * if it was not cancelled nor failed.
     */
    abstract val isActive: Boolean

    /**
     * Cancellation method for coroutine
     */
    abstract fun cancel()

    /**
     * Internal api for coroutine start
     * Only for internal use in extension functions
     */
    internal fun startCoroutine(
        coroutineDispatcher: CoroutineDispatcher,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ) = startCoroutineInternal(coroutineDispatcher, context, block)

    /**
     * Api for coroutine start overloading
     */
    protected abstract fun startCoroutineInternal(
        coroutineDispatcher: CoroutineDispatcher,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    )
}

abstract class AsyncableBaseCoroutineContainer : BaseCoroutineContainer() {
    /**
     * Internal api for coroutine start
     * Only for internal use in extension functions
     */
    internal fun <T> startCoroutineWithResult(
        coroutineDispatcher: CoroutineDispatcher,
        context: DependentCoroutineContext,
        block: suspend CoroutineScope.() -> T,
    ) = startCoroutineWithResultInternal(coroutineDispatcher, context, block)

    /**
     * Api for coroutine start overloading
     */
    protected abstract fun <T> startCoroutineWithResultInternal(
        coroutineDispatcher: CoroutineDispatcher,
        context: DependentCoroutineContext,
        block: suspend CoroutineScope.() -> T,
    ): SimpleDeferred<T>
}