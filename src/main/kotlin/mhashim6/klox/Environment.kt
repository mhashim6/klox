package mhashim6.klox

/**
 *@author mhashim6 on 11/03/19
 */
class Environment {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        return if (values.containsKey(name.lexeme))
            values[name.lexeme]
        else throw RuntimeError(name, "Undefined variable: ${name.lexeme}.")
    }

}