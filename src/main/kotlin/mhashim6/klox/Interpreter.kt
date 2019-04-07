package mhashim6.klox

import mhashim6.klox.LoxError.RuntimeError
import kotlin.math.floor

/**
 *@author mhashim6 on 09/03/19
 */

fun interpret(statements: List<Stmt>, context: Context = Context.create(Environment.natives)) {
    try {
        execute(statements, context.mutate())
    } catch (b: Breakers) {
        when (b) {
            is Breakers.Break -> throw RuntimeError(b.keyword.line, "Illegal use of 'break' outside of a loop .")
            is Breakers.Return -> throw RuntimeError(b.keyword.line, "Illegal use of 'return' outside of a function.")
        }
    }
}

fun execute(statements: List<Stmt>, context: Context): Context {
    var ctx = context
    statements.forEach {
        when (it) {
            is Stmt.Var -> ctx = ctx.mutate(ctx.environment.define(it.name.lexeme, evaluate(it.initializer, ctx).second))
            is Stmt.Fun -> {
                val loxCall = LoxFunction(it, ctx.fork())
                ctx = ctx.mutate(ctx.environment.define(it.name.lexeme, loxCall))
            }
            is Stmt.Return -> throw Breakers.Return(it.keyword, it.value, ctx)
            is Stmt.Expression -> ctx = evaluate(it.expression, ctx).first
            is Stmt.Print -> {
                with(evaluate(it.expression, ctx)) {
                    ctx = first
                    println(stringify(second))
                }
            }
            is Stmt.Block -> ctx = ctx.mutate(execute(it.statements, ctx.fork()).environment)
            is Stmt.If -> {
                val branch = with(evaluate(it.condition, ctx)) {
                    ctx = first
                    if (second.isTruthy()) it.thenBranch
                    else it.elseBranch
                }
                ctx = ctx.mutate(execute(listOf(branch), ctx.fork()).environment)
            }
            is Stmt.While -> {
                var conditionMet = true
                while (conditionMet)
                    with(evaluate(it.condition, ctx)) {
                        ctx = first
                        val condition = second
                        if (condition.isTruthy())
                            try {
                                ctx = execute(listOf(it.body), ctx)
                            } catch (b: Breakers.Break) {
                                conditionMet = false
                            }
                        else conditionMet = false
                    }
            }
            is Stmt.Break -> throw Breakers.Break(it.keyword)
        }
    }
    return ctx
}

private fun stringify(obj: Any?): String = when (obj) {
    null -> "nil"
    is Double ->
        if (floor(obj) == obj) //convert 5.0 => 5
            obj.toInt().toString()
        else obj.toString()
    else -> obj.toString()
}

private fun evaluate(expr: Expr?, context: Context): Pair<Context, Any?> = when (expr) {
    is Expr.Binary -> {
        val (lCtx, left) = evaluate(expr.left, context)
        val (rCtx, right) = evaluate(expr.right, lCtx)
        rCtx to when (expr.operator.type) {

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
                val leftCondition = evaluate(expr.left, rCtx)
                if (leftCondition.isTruthy()) leftCondition else evaluate(expr.right, rCtx)
            }
            TokenType.AND -> {
                val leftCondition = evaluate(expr.left, rCtx)
                if (leftCondition.isTruthy()) evaluate(expr.right, rCtx) else leftCondition
            }
            else -> null  //TODO
        }
    }
    is Expr.Grouping -> with(evaluate(expr.expression, context)) { this.first to this.second }
    is Expr.Literal -> context to expr.value
    is Expr.Unary -> {
        val (ctx, right) = evaluate(expr.right, context)
        when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, right)
                ctx to -(right as Double)
            }
            TokenType.BANG -> ctx to !right.isTruthy()
            else -> ctx to null  //TODO
        }
    }
    is Expr.Call -> {
        var ctx = context
        val function = with(evaluate(expr.callee, ctx)) {
            ctx = first
            if (second !is LoxCall) throw RuntimeError(expr.paren.line, "Can only call functions and classes.")
            else second as LoxCall
        }
        val args = mutableListOf<Any?>()
        expr.arguments.forEach { arg ->
            with(evaluate(arg, ctx)) {
                ctx = first
                args.add(second)
            }
        }
        if (args.size != function.arity)
            throw RuntimeError(expr.paren.line, "Expected ${function.arity} arguments but got ${args.size}.")
        try {
            ctx to function.call(::execute, args)
        } catch (r: Breakers.Return) { //evaluate returns with function's context.
            evaluate(r.value, r.context)
        }
    }
    is Expr.Variable -> context to try {
        context.environment.get(expr.name.lexeme)
    } catch (e: EnvironmentError) {
        throw RuntimeError(expr.name.line, e.message)
    }

    is Expr.Assign -> try {
        with(evaluate(expr.value, context)) {
            first.mutate(first.environment.assign(expr.name.lexeme, second)) to null
        }
    } catch (e: EnvironmentError) {
        throw RuntimeError(expr.name.line, e.message)
    }

    else -> context to null
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

    is String -> when (op2) {
        is String -> op1 + op2
        is Double -> op1 + op2
        else -> throw  RuntimeError(operator.line, "Operands must be numbers or strings.")
    }

    else -> throw  RuntimeError(operator.line, "Operands must be numbers or strings.")
}

interface Context {
    val environment: Environment

    companion object Factory {
        private class ContextImpl(override val environment: Environment) : Context

        fun create(environment: Environment): Context = ContextImpl(environment)
    }
}


fun Context.fork() = mutate(Environment.create(environment))
fun Context.mutate(environment: Environment = Environment.create(this.environment)): Context {
    return Context.create(environment)
}
