package mhashim6.klox

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.immutableMapOf
import kotlinx.collections.immutable.mutate
import mhashim6.klox.LoxCall.Natives.time

/**
 *@author mhashim6 on 11/03/19
 */

interface Environment {
    val closure: Environment?
    fun define(name: String, value: Any?): Environment
    fun assign(name: String, value: Any?): Environment
    fun get(name: String): Any?
    fun contains(name: String): Boolean

    companion object {
        val natives = create(null, immutableMapOf(
                "time" to time
        ))

        fun create(closure: Environment?, values: ImmutableMap<String, Any?> = immutableMapOf()): Environment {
            return EnvironmentImpl(closure, values)
        }
    }
}

private class EnvironmentImpl(override val closure: Environment?, private val values: ImmutableMap<String, Any?>) : Environment {

    override fun define(name: String, value: Any?) = Environment.create(closure,
            values.mutate { it[name] = value }
    )

    override fun assign(name: String, value: Any?): Environment {
        if (!contains(name))
            throw EnvironmentError("Undefined variable: [$name].")

        return define(name, value)
    }

    override fun get(name: String): Any? {
        return when {
            localContains(name) -> values[name]
            contains(name) -> closure!!.get(name)
            else -> throw EnvironmentError("Undefined variable: [$name].")
        }
    }

    override fun toString(): String {
        return "$values ~ $closure"
    }

    override fun contains(name: String): Boolean = localContains(name) || (closure?.contains(name) ?: false)
    private fun localContains(name: String) = values.keys.contains(name)

}

class EnvironmentError(override val message: String) : RuntimeException()