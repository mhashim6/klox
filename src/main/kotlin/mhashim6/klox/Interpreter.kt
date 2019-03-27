package mhashim6.klox

import mhashim6.klox.Environment.Companion.globals
import mhashim6.klox.LoxError.RuntimeError
import kotlin.math.floor

/**
 *@author mhashim6 on 09/03/19
 */

fun interpret(statements: List<Stmt>, environment: Environment) {
    statements.forEach {
        when (it) {
            is Stmt.Var -> environment.define(it.name.lexeme, evaluate(it.initializer, environment))
            is Stmt.Fun -> {
                val loxCall = LoxFunction(it, environment)
                globals.define(it.name.lexeme, loxCall)
            }
            is Stmt.Return -> throw Breakers.Return(it.keyword, it.value)
            is Stmt.Expression -> evaluate(it.expression, environment)
            is Stmt.Print -> println(stringify(evaluate(it.expression, environment)))
            is Stmt.Block -> interpret(it.statements, Environment(enclosing = environment))
            is Stmt.If -> {
                interpret(listOf(
                        if (evaluate(it.condition, environment).isTruthy()) it.thenBranch
                        else it.elseBranch
                ), Environment(enclosing = environment))
            }
            is Stmt.While -> {
                val whileEnv = Environment(enclosing = environment)
                while (evaluate(it.condition, environment).isTruthy())
                    try {
                        interpret(listOf(it.body), whileEnv)
                    } catch (b: Breakers.Break) {
                        break
                    }
            }
            is Stmt.Break -> throw Breakers.Break(it.keyword)
        }
    }
}

private fun stringify(obj: Any?): String = when (obj) {
    null -> "nil"
    is Double ->
        if (floor(obj) == obj) //convert 5.0 => 5
            obj.toInt().toString()
        else obj.toString()
    else -> obj.toString()
}

private fun evaluate(expr: Expr?, environment: Environment): Any? = when (expr) {
    is Expr.Binary -> {
        val left = evaluate(expr.left, environment)
        val right = evaluate(expr.right, environment)
        when (expr.operator.type) {

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                arithmetic(left, right, Double::minus)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                if (right == 0.0) throw RuntimeError(expr.operator.line, "division by zero")
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
            TokenType.OR -> {
                val leftCondition = evaluate(expr.left, environment)
                if (leftCondition.isTruthy()) leftCondition else evaluate(expr.right, environment)
            }
            TokenType.AND -> {
                val leftCondition = evaluate(expr.left, environment)
                if (leftCondition.isTruthy()) evaluate(expr.right, environment) else leftCondition
            }
            else -> null  //TODO
        }
    }
    is Expr.Grouping -> evaluate(expr.expression, environment)
    is Expr.Literal -> expr.value
    is Expr.Unary -> {
        val right = evaluate(expr.right, environment)
        when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, right)
                -(right as Double)
            }
            TokenType.BANG -> !right.isTruthy()
            else -> null  //TODO
        }
    }
    is Expr.Call -> {
        val function = with(evaluate(expr.callee, environment)) {
            if (this !is LoxCall) throw RuntimeError(expr.paren.line, "Can only call functions and classes.")
            else this
        }
        val args = expr.arguments.map { evaluate(it, environment) }
        if (args.size != function.arity) throw RuntimeError(expr.paren.line, "Expected ${function.arity} arguments but got ${args.size}.")
        try {
            function.call(::interpret, args)
        } catch (r: Breakers.Return) {
            evaluate(r.value, environment)
        }

    }
    is Expr.Variable -> try {
        environment.get(expr.name.lexeme)
    } catch (e: EnvironmentError) {
        throw RuntimeError(expr.name.line, e.message)
    }

    is Expr.Assign -> try {
        environment.update(expr.name.lexeme, evaluate(expr.value, environment))
    } catch (e: EnvironmentError) {
        throw RuntimeError(expr.name.line, e.message)
    }

    else -> null
}

private inline fun arithmetic(op1: Any?, op2: Any?, operation: (Double, Double) -> Double): Double {
    return operation(op1 as Double, op2 as Double)
}

private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
    operands.filterNot { it is Double }.ifEmpty { return }
    throw RuntimeError(operator.line, "Operand must be a number.")
}

private fun Any?.isTruthy(): Boolean = when (this) {
    null -> false
    is Boolean -> this
    else -> true
}

private fun isEqual(obj1: Any?, obj2: Any?) = if (obj1 == null) obj2 == null else obj1 == obj2

private fun plus(op1: Any?, op2: Any?, operator: Token): Any = when (op1) {
    is Double -> when (op2) {
        is Double -> op1 + op2
        is String -> op1.toString() + op2
        else -> throw  RuntimeError(operator.line, "Operands must be numbers or strings.")
    }

    is String ->
        when (op2) {
            is String -> op1 + op2
            is Double -> op1 + op2
            else -> throw  RuntimeError(operator.line, "Operands must be numbers or strings.")
        }

    else -> throw  RuntimeError(operator.line, "Operands must be numbers or strings.")
}