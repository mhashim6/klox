package mhashim6.klox


/**
 *@author mhashim6 on 23/03/19
 */

const val MAX_PARAMETERS = 8

class LoxFunction(
        private val declaration: Stmt.Fun,
        private val closure: Environment,
        override val arity: Int = declaration.parameters.size
) : LoxCallable {
    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val env = Environment(closure)
        declaration.parameters.forEachIndexed { index, token -> env.define(token.lexeme, args[index]) }
        try {
            interpreter(listOf(declaration.body), env)
        } catch (r: Breakers.Return) {
            return r.value
        }

        return null //default return type.
    }

    fun bind(loxObject: LoxObject): LoxFunction {
        val env = Environment(closure).apply { define("this", loxObject) }
        return LoxFunction(declaration, env, arity)
    }
}

val time = object : LoxCallable {
    override val arity = 0
    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        return System.currentTimeMillis().toDouble() / 1000.0
    }

}