package mhashim6.klox

import mhashim6.klox.LoxError.RuntimeError


/**
 *@author mhashim6 on 11/04/19
 */

class LoxClass(val name: String, private val methods: MutableMap<String, LoxFunction>) : LoxCallable {

    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val instance = LoxObject(klass = this)
        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        return if (methods.containsKey(name)) {
            methods[name]
        } else null

    }

    override val arity: Int = 0
}

class LoxObject(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()

    operator fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) return fields[name.lexeme]

        klass.findMethod(name.lexeme)?.let {
            return it.bind(this)
        }

        throw RuntimeError(name.line, "Undefined property '${name.lexeme}'.")
    }

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}