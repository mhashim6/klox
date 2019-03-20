package mhashim6.klox

import mhashim6.klox.ParserContext.current
import mhashim6.klox.ParserContext.tokens
import mhashim6.klox.TokenType.*

/**
 *@author mhashim6 on 08/03/19
 */


private object ParserContext {
    var tokens: List<Token> = emptyList()
    var current = 0
}

private val isEOF: Boolean
    get() = peek().type === EOF

private val previous: Token
    get() = tokens[current - 1]


fun parse(tokens: List<Token>): List<Stmt> {
    ParserContext.tokens = tokens
    val statements = mutableListOf<Stmt>()
    while (!isEOF)
        statements.add(declaration())

    return statements

}

private fun declaration(): Stmt {
    return try {
        if (match(VAR)) varDeclaration() else statement()
    } catch (error: ParseError) {
        synchronize()
        Stmt.Empty
    }
}

private fun varDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect variable name.")
    var initializer: Expr? = null
    if (match(EQUAL)) initializer = expression()
    expectSemicolon()
    return Stmt.Var(name, initializer)
}

private fun statement(): Stmt = when {
    match(PRINT) -> printStatement()
    match(LEFT_BRACE) -> block()
    match(IF) -> ifStmt()
    else -> expressionStatement()
}

private fun printStatement(): Stmt {
    val value = expression()
    expectSemicolon()
    return Stmt.Print(value)
}

private fun block(): Stmt {
    val statements = mutableListOf<Stmt>()
    while (!check(RIGHT_BRACE) && !isEOF)
        statements.add(declaration())

    consume(RIGHT_BRACE, "Expect '}' after block.")
    return Stmt.Block(statements)
}

private fun ifStmt(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after 'if'.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after if condition.")

    val thenBranch = statement()
    var elseBranch: Stmt = Stmt.Empty
    if (match(ELSE)) elseBranch = statement()

    return Stmt.IfStmt(condition, thenBranch, elseBranch)
}

private fun expressionStatement(): Stmt {
    val expr = expression()
    expectSemicolon()
    return Stmt.Expression(expr)
}

private fun expectSemicolon() {
    consume(SEMICOLON, "Expect ';' after value.")
}

private fun expression(): Expr {
    return assignment()
}

private fun assignment(): Expr {
    val variable = or()
    return when {
        match(EQUAL) -> {
            val equals = previous
            val value = assignment()
            when (variable) {
                is Expr.Variable -> {
                    val name = variable.name
                    Expr.Assign(name, value)
                }
                else -> throw error(equals, "Invalid assignment target.")
            }
        }
        else -> variable
    }
}

private fun or() = binary(::and, OR)
private fun and() = binary(::equality, AND)
private fun equality() = binary(::comparison, BANG_EQUAL, EQUAL_EQUAL)
private fun comparison() = binary(::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
private fun addition() = binary(::multiplication, MINUS, PLUS)
private fun multiplication() = binary(::unary, SLASH, STAR)

private inline fun binary(operandFactory: () -> Expr, vararg types: TokenType): Expr {
    var expr = operandFactory()

    while (match(*types)) {
        val operator = previous
        val right = operandFactory()
        expr = Expr.Binary(expr, operator, right)
    }
    return expr
}

private fun unary(): Expr {
    if (match(BANG, MINUS)) {
        val operator = previous
        val right = unary()
        return Expr.Unary(operator, right)
    }

    return primary()
}

private fun primary(): Expr = when {
    match(FALSE) -> Expr.Literal(false)
    match(TRUE) -> Expr.Literal(true)
    match(NIL) -> Expr.Literal(null)
    match(STRING, NUMBER) -> Expr.Literal(previous.literal)
    match(IDENTIFIER) -> Expr.Variable(previous)
    match(LEFT_PAREN) -> {
        val expr = expression()
        consume(RIGHT_PAREN, "Expect ')' after expression.")
        Expr.Grouping(expr)
    }
    else -> throw error(peek(), "Expect expression.")
}


private fun match(vararg types: TokenType): Boolean {
    return if (types.firstOrNull(::check) != null) {
        advance()
        true
    } else false
}

private fun consume(type: TokenType, errorMessage: String): Token {
    return if (check(type)) advance() else throw error(peek(), errorMessage)
}

private fun error(token: Token, message: String): ParseError {
    Lox.error(token, message)
    return ParseError()
}

private fun synchronize() {
    advance()

    while (!isEOF) {
        if (previous.type === SEMICOLON) return

        when (peek().type) {
            CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
            else -> advance()
        }
    }
}

private fun check(type: TokenType): Boolean {
    return if (isEOF) false else peek().type == type
}

private fun advance(): Token {
    if (!isEOF) current++
    return previous
}

private fun peek() = tokens[current]

private class ParseError : RuntimeException()
