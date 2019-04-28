package mhashim6.klox

/**
 *@author mhashim6 on 11/04/19
 */

typealias Interpreter = (statements: List<Stmt>, environment: Environment) -> Unit

interface LoxCallable {
    val arity: Int
    fun call(interpreter: Interpreter, args: List<Any?>): Any?
}