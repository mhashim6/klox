package mhashim6.klox

import java.util.*

/**
 *@author mhashim6 on 20/03/19
 */

object ErrorLogs {
    private val scannerErrors: Stack<LoxError.ScannerError> = Stack()
    private val syntaxErrors: Stack<LoxError.SyntaxError> = Stack()
//    private val runtimeErrors: Stack<LoxError.RuntimeError> = Stack()

    val errors: List<LoxError>
        get() = (scannerErrors + syntaxErrors)

    fun log(error: LoxError) {
        when (error) {
            is LoxError.ScannerError -> scannerErrors.push(error)
            is LoxError.SyntaxError -> syntaxErrors.push(error)
//            is LoxError.RuntimeError -> runtimeErrors.push(error)
        }
    }

    fun clear() {
        scannerErrors.clear()
        syntaxErrors.clear()
    }

    val hasSyntaxErrors
        get() = scannerErrors.isNotEmpty() || syntaxErrors.isNotEmpty()

//    val hasRuntimeErrors
//        get() = runtimeErrors.empty()
}

sealed class LoxError(val line: Int, override val message: String) : RuntimeException(message) {
    class RuntimeError(line: Int, message: String) : LoxError(line, message)
    class SyntaxError(val source: Token, message: String) : LoxError(source.line, message)
    class ScannerError(line: Int, message: String) : LoxError(line, message)
}

sealed class Breakers(val keyword: Token) : RuntimeException() {
    class Break(keyword: Token) : Breakers(keyword)
    class Return(keyword: Token, val value: Expr?, val context: Context) : Breakers(keyword)
}