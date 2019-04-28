package mhashim6.klox

import mhashim6.klox.LoxError.ResolverError
import mhashim6.klox.ResolverState.currentScope
import java.util.*


/**
 *@author mhashim6 on 02/04/19
 */


fun resolve(statements: List<Stmt>): Map<Expr, Int> {
    statements.forEach {
        when (it) {
            is Stmt.Expression -> {
                resolveExpr(it.expression)
            }
            is Stmt.Print -> {
                resolveExpr(it.expression)
            }
            is Stmt.Var -> {
                ResolverState.declare(it.name)
                if (it.initializer !== Expr.Empty) resolveExpr(it.initializer);
                ResolverState.define(it.name)
            }
            is Stmt.Fun -> {
                ResolverState.declare(it.name)
                ResolverState.define(it.name)
                resolveFunction(it, ScopeType.FUNCTION)
            }
            is Stmt.Return -> {
                if (currentScope == ScopeType.NONE)
                    throw ResolverError(it.keyword, "Illegal use of 'return' outside of a function.")

                if (it.value != null) resolveExpr(it.value)
            }
            is Stmt.Block -> {
                ResolverState.beginScope()
                resolve(it.statements)
                ResolverState.endScope()
            }
            is Stmt.If -> {
                resolveExpr(it.condition)
                resolve(listOf(it.thenBranch))
                if (it.elseBranch !== Stmt.Empty) resolve(listOf(it.elseBranch))
            }
            is Stmt.While -> {
                resolveExpr(it.condition)
                val prevScope = currentScope
                currentScope = ScopeType.LOOP
                resolve(listOf(it.body))
                currentScope = prevScope
            }

            is Stmt.Break ->
                if (currentScope != ScopeType.LOOP)
                    throw LoxError.RuntimeError(it.keyword.line, "Illegal use of 'break' outside of a loop .")
        }
    }
    return ResolverState.locals
}

private fun resolveFunction(func: Stmt.Fun, type: ScopeType) {
    val prevScope = currentScope
    currentScope = type

    ResolverState.beginScope()
    func.parameters.forEach { param ->
        ResolverState.declare(param)
        ResolverState.define(param)
    }
    resolve(listOf(func.body))
    ResolverState.endScope()
    currentScope = prevScope;
}

private fun resolveExpr(expr: Expr) {
    when (expr) {
        is Expr.Binary -> {
            resolveExpr(expr.left)
            resolveExpr(expr.right)
        }
        is Expr.Grouping -> resolveExpr(expr.expression)
        is Expr.Unary -> resolveExpr(expr.right)
        is Expr.Variable -> {
            if (!ResolverState.scopes.isEmpty() &&
                    ResolverState.scopes.peek()[expr.name.lexeme] == false) {
                throw ResolverError(expr.name, "Cannot read local variable in its own initializer.")
            }
            ResolverState.resolveLocal(expr, expr.name)
        }
        is Expr.Assign -> {
            resolveExpr(expr.value)
            ResolverState.resolveLocal(expr, expr.name)
        }
        is Expr.Call -> {
            resolveExpr(expr.callee)
            expr.arguments.forEach(::resolveExpr)
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
        throw ResolverError(name, "Variable with this name already declared in this scope.");
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

private enum class ScopeType {
    NONE,
    FUNCTION,
    LOOP
}