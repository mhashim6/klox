package mhashim6.klox

import mhashim6.klox.LoxError.ScannerError
import mhashim6.klox.ScannerContext.current
import mhashim6.klox.ScannerContext.lexemeStart
import mhashim6.klox.ScannerContext.line
import mhashim6.klox.ScannerContext.source
import mhashim6.klox.ScannerContext.tokens
import mhashim6.klox.TokenType.*
import java.util.*
import java.lang.Character.MIN_VALUE as nullChar


/**
 * @author mhashim6 on 08/03/19
 */

private object ScannerContext {
    var source: String = ""

    var tokens = ArrayList<Token>()
    var lexemeStart = 0
    var current = 0
    var line = 1

    fun reset(source: String) {
        this.source = source
        tokens.clear()
        lexemeStart = 0
        current = 0
        line = 1
    }
}

private val isEOF: Boolean
    get() = current >= source.length

fun scanTokens(source: String): List<Token> {
    ScannerContext.reset(source)

    while (!isEOF) {
        // We are at the beginning of the next lexeme.
        lexemeStart = current
        scanToken()
    }

    addToken(EOF)
    return tokens
}

private fun scanToken() {
    val c = advance()
    when (c) {
        '(' -> addToken(LEFT_PAREN)
        ')' -> addToken(RIGHT_PAREN)
        '{' -> addToken(LEFT_BRACE)
        '}' -> addToken(RIGHT_BRACE)
        ',' -> addToken(COMMA)
        '.' -> addToken(DOT)
        '-' -> addToken(MINUS)
        '+' -> addToken(PLUS)
        ';' -> addToken(SEMICOLON)
        '*' -> addToken(STAR)
        '!' -> addToken(if (expectMatch('=')) BANG_EQUAL else BANG)
        '=' -> addToken(if (expectMatch('=')) EQUAL_EQUAL else EQUAL)
        '<' -> addToken(if (expectMatch('=')) LESS_EQUAL else LESS)
        '>' -> addToken(if (expectMatch('=')) GREATER_EQUAL else GREATER)
        '/' -> when {
            expectMatch('/') -> lineComment()
            expectMatch('*') -> blockComment()
            else -> addToken(SLASH)
        }
        ' ', '\r', '\t' -> {
            //ignore white spaces.
        }
        '\n' -> line++
        '"' -> string()
        else -> when {
            c.isDigit() -> number()
            c.isAlpha() -> identifier()
            else -> ErrorLogs.log(ScannerError(line, "Unexpected character."))
        }
    }
}

private fun string() {
    while (peek() != '"' && isEOF.not()) {
        if (peek() == '\n') line++
        advance()
    }
    // TODO replace these many peek()s with advance().

    // Unterminated string.
    if (isEOF) {
        ErrorLogs.log(ScannerError(line, "Unterminated string."))
        return
    }

    // The closing ".
    advance()

    // Trim the surrounding quotes.
    val value = source.substring(lexemeStart + 1, current - 1)
    addToken(STRING, value)
}

private fun number() {
    fun readDigits() {
        while (peek().isDigit()) advance()
    }

    readDigits()
    // Look for a fractional part.
    if (peek() == '.' && peek(1).isDigit()) {
        advance() // Consume the "."
        readDigits()
    }

    addToken(NUMBER, source.substring(lexemeStart, current).toDouble())
}

private fun identifier() {
    while (peek().isAlphaNumeric()) advance()
    val lexeme = source.substring(lexemeStart, current)
    addToken(keywords[lexeme] ?: IDENTIFIER)
}

private fun lineComment() {
    while (peek() != '\n' && isEOF.not()) advance()
}

private fun blockComment() {
    while (isEOF.not()) {
        if (peek() == '\n') line++
        when {
            expectMatch('/') ->
                if (expectMatch('*')) //nested comment.
                    blockComment()
                else advance()

            expectMatch('*') ->
                if (expectMatch('/'))  //end of comment.
                    return
                else advance()
            else -> advance()
        }
    }
}

private fun advance(): Char {
    return source.elementAt(current++)
}

private fun peek(lookAhead: Int = 0): Char {
    return if (isEOF || (current + lookAhead) >= source.length) nullChar
    else source.elementAt(current + lookAhead)
}

private fun expectMatch(expected: Char) = when {
    isEOF -> false
    peek() != expected -> false
    else -> {
        current++
        true
    }
}

private fun addToken(type: TokenType, literal: Any? = null) {
    val lexeme = source.substring(lexemeStart, current)
    tokens.add(Token(type, lexeme, literal, line))
}

private fun Char.isAlpha() = this.isLetter() || this == '_'
private fun Char.isAlphaNumeric() = this.isDigit() || this.isAlpha()


val keywords = mapOf<String, TokenType>(
        "and" to AND,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "break" to BREAK,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE)

