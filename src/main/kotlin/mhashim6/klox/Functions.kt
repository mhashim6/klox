package mhashim6.klox


/**
 *@author mhashim6 on 23/03/19
 */

const val MAX_PARAMETERS = 8

typealias Interpreter = (statements: List<Stmt>, context: Context) -> Context

typealias FunctionImplementation = (interpreter: Interpreter, args: List<Any?>) -> Any?

interface LoxCall {
    val arity: Int
    val call: FunctionImplementation

    companion object Natives {
        val time = object : LoxCall {
            override val arity = 0
            override val call: FunctionImplementation = { _, _ ->
                System.currentTimeMillis().toDouble() / 1000.0
            }
        }
    }
}

class LoxFunction(
        private val declaration: Stmt.Fun,
        private val closure: Context,
        override val arity: Int = declaration.parameters.size,
        override val call: FunctionImplementation = { interpreter, args ->
            var scope = closure
            declaration.parameters.forEachIndexed { index, param -> scope = scope.mutate(scope.environment.define(param.lexeme, args[index])) }
            interpreter(listOf(declaration.body), scope)
            null //default return type.
        }
) : LoxCall
