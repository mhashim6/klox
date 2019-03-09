package mhashim6.klox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author mhashim6 on 07/03/19
 */
object Lox {
    private var hadError = false
    private var hadRuntimeError: Boolean = false

    @Throws(IOException::class)
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

    @Throws(IOException::class)
    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (hadError)
            System.exit(65)
        if (hadRuntimeError)
            System.exit(70)
    }

    @Throws(IOException::class)
    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)


        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false
        }
    }

    fun runSource(source: String) {
        run(source)
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        tokens.forEach(::println)
        val parser = Parser(tokens)
        val expression = parser.parse()
        interpret(expression)
        // Stop if there was a syntax error.
        if (hadError) return
        expression?.let { println(PrettyAST.prettify(it)) }
    }

    internal fun error(line: Int, message: String) {
        report(line, "", message)
    }

    internal fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}[line ${error.token.line}]")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println(
                "[line $line] Error $where: $message")
        hadError = true
    }
}