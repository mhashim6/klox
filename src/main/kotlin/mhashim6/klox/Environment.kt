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

    private fun ancestor(distance: Int) = (0 until distance).fold(this) { acc: Environment, _ -> acc.enclosing!! }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun assign(name: String, value: Any?): Unit = when {
        contains(name) -> define(name, value)
        enclosing != null -> enclosing.assign(name, value)
        else -> throw EnvironmentError("Undefined variable: [$name].")
    }

    fun get(name: String): Any? = when {
        contains(name) -> values[name]
        enclosing != null -> enclosing.get(name)
        else -> throw EnvironmentError("Undefined variable: [$name].")
    }

    fun contains(name: String): Boolean = values.keys.contains(name)

    override fun toString(): String {
        return "Env:\n$values\nEnclosing:\n${this.enclosing}"
    }
}

class EnvironmentError(override val message: String) : RuntimeException()