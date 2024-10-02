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

package dev.rakiiii.healthycoroutines.operations.fallback

import dev.rakiiii.healthycoroutines.api.VisibleForExtensions
import dev.rakiiii.healthycoroutines.operations.CancellationHandler
import dev.rakiiii.healthycoroutines.operations.rethrow
import kotlin.coroutines.cancellation.CancellationException

private const val NO_OPERATION = "No operation sat for suspend operation with fallback"
private const val NO_DEFAULT_FALLBACK = "No default fallback strategy sat for suspend operation with fallback"

/**
 * Exception handling helper for doing extra work with result on error
 *
 * Example,
 *
 * ```
 * private const val SOME_DEF_VALUE = ...
 *
 * class SomeViewModel(
 *     private val getSomething: GetSomethingUseCase,
 *     private val getSomethingElse: GetSomethingElseUseCase,
 * ) {
 *
 *      fun someNonTrivialIoOperation() = onIo {
 *         ...
 *
 *         val something = returnOrFallback(getSomething::invoke) {
 *              returnOnError(SOME_DEF_VALUE) {
 *                     getSomethingElse()
 *              }
 *         }
 *
 *         ...
 *      }
 * }
 *
 *      class GetSomethingUseCase() {
 *          suspend fun invoke(): String {...}
 *      }
 *
 *      class GetSomethingElseUseCase() {
 *          suspend fun invoke(): String {...}
 *      }
 * ```
 *
 * @param action Operation
 * @param fallTo Exception handler
 */
suspend inline fun <T : Any> returnOrFallback(
    noinline action: suspend () -> T,
    fallTo: FallbackStrategy<T>,
): T {
    return returnOrFallback {
        action(action)
        otherFallTo(fallTo)
    }
}

/**
 * Exception handling helper for typed exception handing strategies
 *
 * Example,
 *
 * ```
 * private const val SOME_DEF_VALUE = ...
 *
 * class SomeViewModel(
 *     private val getSomething: GetSomethingUseCase,
 *     private val getSomethingElse: GetSomethingElseUseCase,
 *     private val getOtherOne: GetOtherOneUseCase,
 * ) {
 *
 *     fun someNonTrivialIoOperation(id: String) = onIo {
 *         ...
 *
 *         val something = returnOrFallback {
 *             action { getSomething(id) }
 *
 *             on<SomeException1>() fallTo { getSomethingElse(id) }
 *             on<SomeException2>() fallTo {
 *                 returnOnError(SOME_DEF_VALUE) {
 *                     getOtherOne(id)
 *                 }
 *             }
 *
 *             otherFallTo { dieStrategy() }
 *         }
 *
 *         ...
 *     }
 *
 *     fun dieStrategy() {...}
 *
 * }
 *     class GetSomethingUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 *
 *     class GetSomethingElseUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 *
 *     class GetOtherOneUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 * ```
 */
suspend inline fun <T : Any> returnOrFallback(
    noinline actionBuilder: FallbackSchemaBuilder<T>.() -> Unit,
): T {
    return returnOrFallback(
        exit = { throw IllegalStateException("Impossible due to design") },
        actionBuilder = actionBuilder,
    )
}

/**
 * Type based [FallbackPredicate] method for [returnOrFallback]
 */
inline fun <reified T : Throwable> on() = FallbackPredicate { it is T }

/**
 * Lambda based [FallbackPredicate] method for [returnOrFallback]
 */
inline fun on(crossinline predicate: (Throwable) -> Boolean) = FallbackPredicate {
    predicate(it)
}

/**
 * Exception handling helper for typed exception handing strategies with support of parent function ending behavior
 *
 * Example,
 *
 * ```
 * private const val SOME_DEF_VALUE = ...
 *
 * class SomeViewModel(
 *     private val getSomething: GetSomethingUseCase,
 *     private val getSomethingElse: GetSomethingElseUseCase,
 *     private val getOtherOne: GetOtherOneUseCase,
 * ) {
 *
 *     fun someNonTrivialIoOperation(id: String) = onIo {
 *         ...
 *
 *         val something = returnOrFallback(exit = { return@onIo }) {
 *             action { getSomething(id) }
 *
 *             on<SomeException1>() fallTo { getSomethingElse(id) }
 *             on<SomeException2>() fallToExitAfter { hideLoading() }
 *             on<SomeException4>() fallTo {
 *                 returnOnError(SOME_DEF_VALUE) {
 *                     getOtherOne(id)
 *                 }
 *             }
 *             fallToExitOn<_, SomeException3>()
 *
 *             otherFallTo { dieStrategy() }
 *         }
 *
 *         ...
 *     }
 *
 *     fun dieStrategy() {...}
 *     fun hideLoading() {...}
 * }
 *
 *     class GetSomethingUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 *
 *     class GetSomethingElseUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 *
 *     class GetOtherOneUseCase() {
 *         suspend fun invoke(id: String): String {...}
 *     }
 * ```
 */
@OptIn(VisibleForExtensions::class)
suspend inline fun <T : Any> returnOrFallback(
    exit: (Throwable) -> Nothing,
    noinline actionBuilder: FallbackSchemaWithExitBuilder<T>.() -> Unit,
): T {
    val strategy = fallbackSchema(actionBuilder)
    return try {
        strategy.operation()
    } catch (e: CancellationException) {
        strategy.cancel(e)
    } catch (e: Throwable) {
        when (val onErrorStrategy = strategy.onError(e)) {
            is Either.Left -> onErrorStrategy.value
            is Either.Right -> exit(e)
        }
    }
}

/**
 * Type based selected [FallbackStrategy] with only exit from parent function on error
 */
inline fun <reified T : Any, reified E : Throwable> FallbackSchemaWithExitBuilder<T>.fallToExitOn() {
    on<E>() fallToExitAfter {}
}

/** Api for fallback behavior on error */
fun interface FallbackStrategy<T> {
    suspend operator fun invoke(t1: Throwable): T
}

/** Api for fallback behavior on error that leads to end of parent function after execution */
fun interface ExitStrategy {
    suspend operator fun invoke(t1: Throwable)
}

/** Api for mapping errors to handlers */
fun interface FallbackPredicate {
    operator fun invoke(t: Throwable): Boolean
}

/**
 * Api for building error handling schema
 */
interface FallbackSchemaBuilder<T> {
    /** Operation which errors should be handled */
    fun action(operation: suspend () -> T)
    /** Default [FallbackStrategy] will be called if no other [FallbackStrategy] is matching error */
    fun otherFallTo(fallbackOperation: FallbackStrategy<T>)
    /** Function to add [FallbackStrategy] with matching [FallbackPredicate] */
    infix fun FallbackPredicate.fallTo(fallbackStrategy: FallbackStrategy<T>)
    /** [CancellationException] handler, by default rethrow error */
    fun onCancel(operation: CancellationHandler<T>)
}

/**
 * Api for building error handling schema with support of ending parent function
 */
interface FallbackSchemaWithExitBuilder<T> : FallbackSchemaBuilder<T> {
    /** Function to add [ExitStrategy] with matching [FallbackPredicate] */
    infix fun FallbackPredicate.fallToExitAfter(exitStrategy: ExitStrategy)
}

@VisibleForExtensions
fun <T : Any> fallbackSchema(builder: FallbackSchemaWithExitBuilder<T>.() -> Unit): FallbackSchema<T> {
    return FallbackSchemaBuilderImpl<T>().apply(builder)
}

@VisibleForExtensions
interface FallbackSchema<T : Any> {
    val operation: (suspend () -> T)
    val cancel: CancellationHandler<T>
    suspend fun onError(error: Throwable): Either<T, Unit>
}

@OptIn(VisibleForExtensions::class)
private class FallbackSchemaBuilderImpl<T : Any> : FallbackSchema<T>, FallbackSchemaWithExitBuilder<T> {

    private var _operation: (suspend () -> T) = { throw IllegalStateException(NO_OPERATION) }
    private var _defaultOnError: FallbackStrategy<T> = noDefaultFallbackStrategy()
    private var _cancel: CancellationHandler<T> = rethrow()
    private val onError = onErrorInit()

    override val operation: (suspend () -> T) get() = _operation
    override val cancel: CancellationHandler<T> get() = _cancel

    override suspend fun onError(error: Throwable): Either<T, Unit> {
        val handler = onError.find { (isError, _) -> isError(error) }?.second
        return handler?.mapLeft { it(error) }?.map { it(error) } ?: Either.Left(_defaultOnError(error))
    }

    override fun action(operation: suspend () -> T) {
        _operation = operation
    }

    override fun otherFallTo(fallbackOperation: FallbackStrategy<T>) {
        _defaultOnError = fallbackOperation
    }

    override fun FallbackPredicate.fallTo(fallbackStrategy: FallbackStrategy<T>) {
        onError.add(this to Either.Left(fallbackStrategy))
    }

    override fun FallbackPredicate.fallToExitAfter(exitStrategy: ExitStrategy) {
        onError.add(this to Either.Right(exitStrategy))
    }

    override fun onCancel(operation: CancellationHandler<T>) {
        _cancel = operation
    }

    private fun <T> noDefaultFallbackStrategy() = FallbackStrategy<T> {
        throw IllegalStateException(NO_DEFAULT_FALLBACK)
    }

    private fun onErrorInit() =
        mutableListOf<Pair<FallbackPredicate, Either<FallbackStrategy<T>, ExitStrategy>>>()
}

@VisibleForExtensions
sealed class Either<out A, out B> {
    data class Left<out A>(val value: A) : Either<A, Nothing>() {
        override fun toString(): String = "Either.Left($value)"
    }

    data class Right<out B>(val value: B) : Either<Nothing, B>() {
        override fun toString(): String = "Either.Left($value)"
    }

    inline fun <A, B, C> Either<A, B>.flatMap(f: (right: B) -> Either<A, C>): Either<A, C> {
        return when (this) {
            is Right -> f(this.value)
            is Left -> this
        }
    }

    inline fun <C> mapLeft(f: (A) -> C): Either<C, B> {
        return when (this) {
            is Left -> Left(f(value))
            is Right -> Right(value)
        }
    }

    inline fun <C> map(f: (right: B) -> C): Either<A, C> {
        return flatMap { Right(f(it)) }
    }
}
