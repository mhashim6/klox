package mhashim6.klox

import java.util.ArrayList
import mhashim6.klox.TokenType.*
import java.lang.Character.MIN_VALUE as nullChar


/**
 * @author mhashim6 on 08/03/19
 */
internal class Scanner(private val source: String) {
    private val tokens = ArrayList<Token>()
    private var lexemeStart = 0
    private var current = 0
    private var line = 1

    private val isEOF: Boolean
        get() = current >= source.length

    @Suppress("UNREACHABLE_CODE")
    fun scanTokens(): List<Token> {
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
                else -> Lox.error(line, "Unexpected character.")
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
            Lox.error(line, "Unterminated string.")
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


    companion object {
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
                "prettify" to PRINT,
                "return" to RETURN,
                "super" to SUPER,
                "this" to THIS,
                "true" to TRUE,
                "var" to VAR,
                "while" to WHILE)
    }

}
