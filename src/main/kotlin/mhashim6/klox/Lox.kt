package mhashim6.klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author mhashim6 on 07/03/19
 */
object Lox {
    private var hadRuntimeError: Boolean = false

    @JvmStatic
    fun main(args: Array<String>) {
        when {
            args.size > 1 -> {
                println("Usage: klox [script]")
                System.exit(64)
            }
            args.size == 1 -> runFile(args[0])
            else -> runPrompt()
        }
    }

    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (ErrorLogs.hasSyntaxErrors)
            System.exit(65)
        if (hadRuntimeError)
            System.exit(70)
    }

    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)


        while (true) {
            print("> ")
            run(reader.readLine())
        }
    }

    private val environment = Environment()
    fun run(source: String) {
        try {
            val tokens = scanTokens(source)
            val statements = parse(tokens)
            if (ErrorLogs.hasSyntaxErrors) {
                ErrorLogs.errors.forEach(::error)
                ErrorLogs.clear()
                return
            }

            interpret(statements, environment)
        } catch (err: LoxError.RuntimeError) {
            error(err)
        }
    }

    private fun error(err: LoxError) {
        when (err) {
            is LoxError.ScannerError -> scannerError(err.line, err.message)
            is LoxError.SyntaxError -> syntaxError(err.source, err.message)
            is LoxError.RuntimeError -> runtimeError(err)
        }
    }

    private fun scannerError(line: Int, message: String) {
        report(line, "", message)
    }

    private fun syntaxError(token: Token, message: String) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    private fun runtimeError(error: LoxError.RuntimeError) {
        System.err.println("${error.message} @ [line: ${error.line}]")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println(
                "[line $line] Error $where: $message")
    }
}