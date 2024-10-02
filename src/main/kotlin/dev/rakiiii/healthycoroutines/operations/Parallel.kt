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

import dev.rakiiii.healthycoroutines.api.CoroutineScopeHolder
import dev.rakiiii.healthycoroutines.api.defaults.DefaultDependentCoroutineContextConverter
import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import dev.rakiiii.healthycoroutines.api.dependentcontext.completionExceptionHandler
import dev.rakiiii.healthycoroutines.functional.async
import dev.rakiiii.healthycoroutines.functional.await
import dev.rakiiii.healthycoroutines.functional.traverse
import dev.rakiiii.healthycoroutines.handlers.logAndRethrowError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

//region Computation
/**
 * Function for parallel execution of 3 operations on [Dispatchers.Default]
 */
suspend inline fun <T1, T2, T3> CoroutineScopeHolder.runComputationInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    crossinline action3: suspend () -> T3,
): Triple<T1, T2, T3> {
    return coroutineScope.runComputationInParallel(context, action1, action2, action3)
}

/**
 * Function for parallel execution of 2 operations on [Dispatchers.Default]
 */
suspend inline fun <T1, T2> CoroutineScopeHolder.runComputationInParallel(
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
): Pair<T1, T2> {
    return coroutineScope.runComputationInParallel(context, action1, action2)
}

/**
 * Function for parallel execution of any amount of operations on [Dispatchers.Default]
 */
suspend inline fun <R : Any> CoroutineScopeHolder.runComputationListInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    vararg actions: suspend () -> R,
): List<R> {
    return runComputationListInParallel(context, actions.toList())
}

/**
 * Function for parallel execution of list of operations on [Dispatchers.Default]
 */
suspend inline fun <R : Any> CoroutineScopeHolder.runComputationListInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    actions: List<suspend () -> R>,
): List<R> {
    return runComputationListInParallel(coroutineScope, context, actions)
}

/**
 * Function for parallel execution of 3 operations on [Dispatchers.Default]
 */
suspend inline fun <T1, T2> CoroutineScope.runComputationInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
): Pair<T1, T2> {
    val deferred1 = async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context)) { action1() }
    val deferred2 = async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context)) { action2() }

    return deferred1.await() to deferred2.await()
}

/**
 * Function for parallel execution of 2 operations on [Dispatchers.Default]
 */
suspend inline fun <T1, T2, T3> CoroutineScope.runComputationInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    crossinline action3: suspend () -> T3,
): Triple<T1, T2, T3> {
    val deferred1 = async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context)) { action1() }
    val deferred2 = async(DefaultDependentCoroutineContextConverter(context)) { action2() }
    val deferred3 = async(DefaultDependentCoroutineContextConverter(context)) { action3() }

    return Triple(deferred1.await(), deferred2.await(), deferred3.await())
}

/**
 * Function for parallel execution of any amount of operations on [Dispatchers.Default]
 */
suspend inline fun <R : Any> runComputationListInParallel(
    scope: CoroutineScope,
    context: DependentCoroutineContext,
    vararg actions: suspend () -> R,
): List<R> = runComputationListInParallel(scope, context, actions.toList())

/**
 * Function for parallel execution of list of operations on [Dispatchers.Default]
 */
suspend inline fun <R : Any> runComputationListInParallel(
    scope: CoroutineScope,
    context: DependentCoroutineContext,
    actions: List<suspend () -> R>,
): List<R> {
    return actions
        .traverse(async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context), scope) { it() })
        .traverse(await())
}
//endregion

//region Io
/**
 * Function for parallel execution of 2 operations on [Dispatchers.IO]
 */
suspend inline fun <T1, T2> CoroutineScopeHolder.runIoInParallel(
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
): Pair<T1, T2> {
    return coroutineScope.runIoInParallel(context, action1, action2)
}

/**
 * Function for parallel execution of 3 operations on [Dispatchers.IO]
 */
suspend inline fun <T1, T2, T3> CoroutineScopeHolder.runIoInParallel(
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    crossinline action3: suspend () -> T3,
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
): Triple<T1, T2, T3> {
    return coroutineScope.runIoInParallel(context, action1, action2, action3)
}

/**
 * Function for parallel execution of any amount of operations on [Dispatchers.IO]
 */
suspend inline fun <R : Any> CoroutineScopeHolder.runIoListInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    vararg actions: suspend () -> R,
): List<R> {
    return runIoListInParallel(context, actions.toList())
}

/**
 * Function for parallel execution of list of operations on [Dispatchers.IO]
 */
suspend inline fun <R : Any> CoroutineScopeHolder.runIoListInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    actions: List<suspend () -> R>,
): List<R> {
    return runIoListInParallel(coroutineScope, context, actions)
}

/**
 * Function for parallel execution of 2 operations on [Dispatchers.IO]
 */
suspend inline fun <T1, T2> CoroutineScope.runIoInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
): Pair<T1, T2> {
    val deferred1 = async(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context)) { action1() }
    val deferred2 = async(DefaultDependentCoroutineContextConverter(context)) { action2() }

    return deferred1.await() to deferred2.await()
}

/**
 * Function for parallel execution of 3 operations on [Dispatchers.IO]
 */
suspend inline fun <T1, T2, T3> CoroutineScope.runIoInParallel(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline action1: suspend () -> T1,
    crossinline action2: suspend () -> T2,
    crossinline action3: suspend () -> T3,
): Triple<T1, T2, T3> {
    val deferred1 = async(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context)) { action1() }
    val deferred2 = async(DefaultDependentCoroutineContextConverter(context)) { action2() }
    val deferred3 = async(DefaultDependentCoroutineContextConverter(context)) { action3() }

    return Triple(deferred1.await(), deferred2.await(), deferred3.await())
}

/**
 * Function for parallel execution of any amount of operations on [Dispatchers.IO]
 */
suspend inline fun <R : Any> runIoListInParallel(
    scope: CoroutineScope,
    context: DependentCoroutineContext,
    vararg actions: suspend () -> R,
): List<R> = runIoListInParallel(scope, context, actions.toList())

/**
 * Function for parallel execution of list of operations on [Dispatchers.IO]
 */
suspend inline fun <R : Any> runIoListInParallel(
    scope: CoroutineScope,
    context: DependentCoroutineContext,
    actions: List<suspend () -> R>,
): List<R> {
    return actions
        .traverse(async(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context), scope) { it() })
        .traverse(await())
}
//endregion
