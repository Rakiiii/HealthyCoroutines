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

package dev.rakiiii.healthycoroutines.api.dependentcontext

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Api for special coroutine context for not StandalneCoroutine coroutine
 */
interface DependentCoroutineContext {
    operator fun <E : Element> get(key: Key<E>): E?
    fun <R> fold(initial: R, operation: (R, Element) -> R): R
    operator fun plus(context: DependentCoroutineContext): DependentCoroutineContext {
        return if (context === EmptyDependentCoroutineContext) {
            this
        } else {
            context.fold(this) { acc, element ->
                val removed = acc.minusKey(element.key)
                if (removed === EmptyDependentCoroutineContext) {
                    element
                } else {
                    CombinedDependentCoroutineContext(removed, element)
                }
            }
        }
    }

    fun minusKey(key: Key<*>): DependentCoroutineContext

    interface Key<E : Element>
    interface Element : DependentCoroutineContext {
        val key: Key<*>
        operator fun not(): CoroutineContext

        override operator fun <E : Element> get(key: Key<E>): E? =
            if (this.key == key) this as E else null

        public override fun <R> fold(initial: R, operation: (R, Element) -> R): R =
            operation(initial, this)

        public override fun minusKey(key: Key<*>): DependentCoroutineContext =
            if (this.key == key) EmptyDependentCoroutineContext else this
    }
}

fun DependentCoroutineContext.Element?.contextOrEmpty() = this?.not() ?: EmptyCoroutineContext

private class CombinedDependentCoroutineContext(
    private val left: DependentCoroutineContext,
    private val element: DependentCoroutineContext.Element,
) : DependentCoroutineContext {
    override fun <E : DependentCoroutineContext.Element> get(key: DependentCoroutineContext.Key<E>): E? {
        var cur = this
        while (true) {
            cur.element[key]?.let { return it }
            val next = cur.left
            if (next is CombinedDependentCoroutineContext) {
                cur = next
            } else {
                return next[key]
            }
        }
    }

    override fun <R> fold(initial: R, operation: (R, DependentCoroutineContext.Element) -> R): R {
        return operation(left.fold(initial, operation), element)
    }

    override fun minusKey(key: DependentCoroutineContext.Key<*>): DependentCoroutineContext {
        if (element[key] != null) return left
        val newLeft = left.minusKey(key)
        return when {
            newLeft === left -> this
            newLeft === EmptyDependentCoroutineContext -> element
            else -> CombinedDependentCoroutineContext(newLeft, element)
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
                other is CombinedDependentCoroutineContext &&
                other.size() == size() &&
                other.containsAll(this)

    override fun hashCode(): Int = left.hashCode() + element.hashCode()

    override fun toString(): String = "[$left, $element]"

    private fun size(): Int {
        var cur = this
        var size = 2
        while (true) {
            cur = cur.left as? CombinedDependentCoroutineContext ?: return size
            size++
        }
    }

    private fun contains(element: DependentCoroutineContext.Element): Boolean =
        get(element.key) == element

    private fun containsAll(context: CombinedDependentCoroutineContext): Boolean {
        var cur = context
        while (true) {
            if (!contains(cur.element)) return false
            val next = cur.left
            if (next is CombinedDependentCoroutineContext) {
                cur = next
            } else {
                return contains(next as DependentCoroutineContext.Element)
            }
        }
    }
}