package mhashim6.klox

/**
 *@author mhashim6 on 11/03/19
 */
class Environment(private val enclosing: Environment?) {
    private val values = mutableMapOf<String, Any?>()

    fun define(variable: Pair<String, Any?>) = define(variable.first, variable.second)

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun update(name: String, value: Any?): Unit = when {
        localContains(name) -> define(name, value)
        contains(name) -> enclosing!!.update(name, value)
        else -> throw EnvironmentError("Undefined variable: [$name].")
    }

    fun get(name: String): Any? = when {
        localContains(name) -> values[name]
        contains(name) -> enclosing!!.get(name)
        else -> throw EnvironmentError("Undefined variable: [$name].")
    }

    fun contains(name: String): Boolean = localContains(name) || (enclosing?.contains(name) ?: false)
    private fun localContains(name: String) = values.keys.contains(name)

    companion object {
        val globals = Environment(null).apply {
            define("time", time) //TODO
        }
    }
}

class EnvironmentError(override val message: String) : RuntimeException()