package mhashim6.klox

import java.util.*

/**
 *@author mhashim6 on 20/03/19
 */

object ErrorLogs {
    private val log: Stack<LoxError> = Stack()

    val errors: List<LoxError>
        get() = log

    fun log(error: LoxError) {
        log.push(error)
    }

    fun clear() = log.clear()

    val hasErrors
        get() = log.isNotEmpty()
}

sealed class LoxError(val line: Int, override val message: String) : RuntimeException(message) {
    class RuntimeError(line: Int, message: String) : LoxError(line, message)
    class ResolverError(val source: Token, message: String) : LoxError(source.line, message)
    class SyntaxError(val source: Token, message: String) : LoxError(source.line, message)
    class ScannerError(line: Int, message: String) : LoxError(line, message)
}

sealed class Breakers(val keyword: Token) : RuntimeException() {
    class Break(keyword: Token) : Breakers(keyword)
    class Return(keyword: Token, val value: Expr?, val environment: Environment) : Breakers(keyword)
}