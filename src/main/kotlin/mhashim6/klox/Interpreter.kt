package mhashim6.klox

import kotlin.math.floor

/**
 *@author mhashim6 on 09/03/19
 */

fun interpret(expr: Expr?) {
    try {
        val result = evaluate(expr)
        println(stringify(result))
    } catch (error: RuntimeError) {
        Lox.runtimeError(error)
    }
}

fun stringify(obj: Any?): String = when (obj) {
    null -> "nil"
    is Double ->
        if (floor(obj) == obj) //convert 5.0 => 5
            obj.toInt().toString()
        else obj.toString()
    else -> obj.toString()
}

fun evaluate(expr: Expr?): Any? = when (expr) {
    is Expr.Binary -> {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        when (expr.operator.type) {

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                arithmetic(left, right, Double::minus)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                arithmetic(left, right, Double::div)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                arithmetic(left, right, Double::times)
            }
            TokenType.PLUS -> plus(left, right, expr.operator)

            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            else -> null  //TODO
        }
    }
    is Expr.Grouping -> evaluate(expr.expression)
    is Expr.Literal -> expr.value
    is Expr.Unary -> {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, right)
                -(right as Double)
            }
            TokenType.BANG -> !(isTruthy(right))
            else -> null  //TODO
        }
    }
    else -> null
}

inline fun arithmetic(op1: Any?, op2: Any?, operation: (Double, Double) -> Double): Double {
    return operation(op1 as Double, op2 as Double)
}

private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
    operands.filterNot { it is Double }.ifEmpty { return }
    throw RuntimeError(operator, "Operand must be a number.")
}

fun isTruthy(obj: Any?): Boolean = when (obj) {
    null -> false
    is Boolean -> obj
    else -> false
}

fun isEqual(obj1: Any?, obj2: Any?) = if (obj1 == null) obj2 == null else obj1 == obj2

fun plus(op1: Any?, op2: Any?, operator: Token): Any = when {
    op1 is Double && op2 is Double -> op1 + op2
    op1 is String && op2 is String -> op1 + op2
    else -> throw  RuntimeError(operator, "Operands must be two numbers or two strings.");
}