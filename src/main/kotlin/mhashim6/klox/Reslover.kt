package mhashim6.klox

import mhashim6.klox.LoxError.ResolverError
import mhashim6.klox.ResolverState.currentScope
import java.util.*


/**
 *@author mhashim6 on 02/04/19
 */


fun resolve(statements: List<Stmt>, scopeType: ScopeType = ScopeType.NONE): Map<Expr, Int> {
    statements.forEach {
        when (it) {
            is Stmt.Expression -> {
                resolveExpr(it.expression, scopeType)
            }
            is Stmt.Print -> {
                resolveExpr(it.expression, scopeType)
            }
            is Stmt.Var -> {
                ResolverState.declare(it.name)
                if (it.initializer !== Expr.Empty) resolveExpr(it.initializer, scopeType)
                ResolverState.define(it.name)
            }
            is Stmt.Fun -> {
                ResolverState.declare(it.name)
                ResolverState.define(it.name)
                resolveFunction(it)
            }
            is Stmt.Class -> {
                ResolverState.declare(it.name)
                ResolverState.define(it.name)

                ResolverState.beginScope()
                ResolverState.scopes.peek()["this"] = true

                it.methods.forEach { method -> resolveFunction(method, ScopeType.METHOD) }
                ResolverState.endScope()
            }
            is Stmt.Return -> {
                if (scopeType == ScopeType.NONE)
                    ErrorLogs.log(ResolverError(it.keyword, "Illegal use of 'return' outside of a function."))

                if (it.value != null) resolveExpr(it.value, scopeType)
            }
            is Stmt.Block -> {
                ResolverState.beginScope()
                resolve(it.statements, scopeType)
                ResolverState.endScope()
            }
            is Stmt.If -> {
                resolveExpr(it.condition, scopeType)
                resolve(listOf(it.thenBranch), scopeType)
                if (it.elseBranch !== Stmt.Empty) resolve(listOf(it.elseBranch), scopeType)
            }
            is Stmt.While -> {
                resolveExpr(it.condition, scopeType)
                resolve(listOf(it.body), ScopeType.LOOP)
            }

            is Stmt.Break ->
                if (currentScope != ScopeType.LOOP)
                    ErrorLogs.log(LoxError.RuntimeError(it.keyword.line, "Illegal use of 'break' outside of a loop ."))
        }
    }
    return ResolverState.locals
}

private fun resolveFunction(func: Stmt.Fun, scopeType: ScopeType = ScopeType.FUNCTION) {
    ResolverState.beginScope()
    func.parameters.forEach { param ->
        ResolverState.declare(param)
        ResolverState.define(param)
    }
    resolve(listOf(func.body), scopeType)
    ResolverState.endScope()
}

private fun resolveExpr(expr: Expr, scopeType: ScopeType) {
    when (expr) {
        is Expr.Binary -> {
            resolveExpr(expr.left, scopeType)
            resolveExpr(expr.right, scopeType)
        }
        is Expr.Grouping -> resolveExpr(expr.expression, scopeType)
        is Expr.Unary -> resolveExpr(expr.right, scopeType)
        is Expr.Variable -> {
            if (!ResolverState.scopes.isEmpty() &&
                    ResolverState.scopes.peek()[expr.name.lexeme] == false) {
                ErrorLogs.log(ResolverError(expr.name, "Cannot read local variable in its own initializer."))
            }
            ResolverState.resolveLocal(expr, expr.name)
        }
        is Expr.Assign -> {
            resolveExpr(expr.value, scopeType)
            ResolverState.resolveLocal(expr, expr.name)
        }
        is Expr.Call -> {
            resolveExpr(expr.callee, scopeType)
            expr.arguments.forEach { resolveExpr(it, scopeType) }
        }
        is Expr.Get -> resolveExpr(expr.loxObject, scopeType)
        is Expr.Set -> {
            resolveExpr(expr.value, scopeType)
            resolveExpr(expr.loxObject, scopeType)
        }
        is Expr.This -> {
            if (scopeType == ScopeType.CLASS || scopeType == ScopeType.METHOD)
                ResolverState.resolveLocal(expr, expr.keyword)
            else
                ErrorLogs.log(ResolverError(expr.keyword, "Cannot use 'this' outside of a class."))

        }
    }
}

private object ResolverState {
    val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    val locals: MutableMap<Expr, Int> = mutableMapOf()
    var currentScope = ScopeType.NONE
}

private fun ResolverState.declare(name: Token) {
    if (scopes.isEmpty()) return //we are global!

    val scope = scopes.peek()

    if (scope.containsKey(name.lexeme)) {
        ErrorLogs.log(ResolverError(name, "Variable with this name already declared in this scope."))
    }

    scope[name.lexeme] = false
}

private fun ResolverState.define(name: Token) {
    if (scopes.isEmpty()) return
    scopes.peek()[name.lexeme] = true
}

private fun ResolverState.beginScope() {
    scopes.push(mutableMapOf())
}

private fun ResolverState.endScope() {
    scopes.pop()
}

private fun ResolverState.resolveLocal(expr: Expr, name: Token) {
    (scopes.size - 1 downTo 0).firstOrNull { scopes[it].containsKey(name.lexeme) }?.also {
        locals[expr] = scopes.size - 1 - it
        return
    }
    //we are global!
}

enum class ScopeType {
    NONE,
    FUNCTION,
    METHOD,
    CLASS,
    LOOP
}