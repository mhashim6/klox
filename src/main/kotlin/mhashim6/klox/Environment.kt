package mhashim6.klox

/**
 *@author mhashim6 on 11/03/19
 */
class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? = when {
        contains(name) -> values[name.lexeme]
        enclosing != null -> enclosing.get(name)
        else -> throw RuntimeError(name, "Undefined variable: ${name.lexeme}.")
    }

    fun contains(name: Token) = values.keys.contains(name.lexeme)

}