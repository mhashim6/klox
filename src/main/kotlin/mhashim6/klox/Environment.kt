package mhashim6.klox

/**
 *@author mhashim6 on 11/03/19
 */
class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: String): Any? = when {
        contains(name) -> values[name]
        enclosing != null -> enclosing.get(name)
        else -> throw EnvironmentError("Undefined variable: [$name].")
    }

    fun contains(name: String) = values.keys.contains(name)

}

class EnvironmentError(override val message: String) : RuntimeException()