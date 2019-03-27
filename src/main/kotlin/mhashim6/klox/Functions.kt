package mhashim6.klox


/**
 *@author mhashim6 on 23/03/19
 */

const val MAX_PARAMETERS = 8

typealias Interpreter = (statements: List<Stmt>, environment: Environment) -> Unit

typealias FunctionImplementation = (interpreter: Interpreter, args: List<Any?>) -> Any?

interface LoxCall {
    val arity: Int
    val call: FunctionImplementation
}

class LoxFunction(
        private val declaration: Stmt.Fun,
        private val closure: Environment,
        override val arity: Int = declaration.parameters.size,
        override val call: FunctionImplementation = { interpreter, args ->
            val env = Environment(closure)
            declaration.parameters.forEachIndexed { index, token -> env.define(token.lexeme, args[index]) }
            interpreter(listOf(declaration.body), env)
            null //default return type.
        }
) : LoxCall

val time = object : LoxCall {
    override val arity = 0
    override val call: FunctionImplementation = { _, _ ->
        System.currentTimeMillis().toDouble() / 1000.0
    }

}