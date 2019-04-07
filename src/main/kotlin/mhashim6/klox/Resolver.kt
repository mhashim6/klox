package mhashim6.klox

import java.util.*


/**
 *@author mhashim6 on 02/04/19
 */


fun resolve(statements: List<Stmt>, state: ResolverState = ResolverState.create()): HashMap<Expr, Int> {
    statements.forEach {
        when (it) {
            is Stmt.Expression -> {
                resolveExpr(it.expression, state)
            }
            is Stmt.Print -> {
                resolveExpr(it.expression, state)
            }
            is Stmt.Var -> {
                state.declare(it.name)
                if (it.initializer !== Expr.Empty) resolveExpr(it.initializer, state);
                state.define(it.name)
            }
            is Stmt.Fun -> {
                state.declare(it.name)
                state.define(it.name)
                resolveFunction(it, state)
            }
            is Stmt.Return -> if (it.value != null) resolveExpr(it.value, state)
            is Stmt.Block -> {
                state.beginScope()
                resolve(it.statements, state)
                state.endScope()
            }
            is Stmt.If -> {
                resolveExpr(it.condition, state)
                resolve(listOf(it.thenBranch), state)
                if (it.elseBranch !== Stmt.Empty) resolve(listOf(it.elseBranch), state)
            }
            is Stmt.While -> {
                resolveExpr(it.condition, state)
                resolve(listOf(it.body), state)
            }
        }
    }
    return state.locals
}

private fun resolveFunction(func: Stmt.Fun, state: ResolverState) {
    state.beginScope()
    func.parameters.forEach { param ->
        state.declare(param)
        state.define(param)
    }
    resolve(listOf(func.body), state)
    state.endScope()
}

private fun resolveExpr(expr: Expr, state: ResolverState) {
    when (expr) {
        is Expr.Binary -> {
            resolveExpr(expr.left, state)
            resolveExpr(expr.right, state)
        }
        is Expr.Grouping -> resolveExpr(expr.expression, state)
        is Expr.Unary -> resolveExpr(expr.right, state)
        is Expr.Variable -> {
            state.resolveLocal(expr, expr.name)
        }
        is Expr.Assign -> {
            resolveExpr(expr.value, state)
            state.resolveLocal(expr, expr.name)
        }
        is Expr.Call -> {
            resolveExpr(expr.callee, state)
            expr.arguments.forEach { resolveExpr(it, state) }
        }
    }
}

//TODO immutability.
interface ResolverState {
    val scopes: Stack<MutableMap<String, Boolean>>
    val locals: HashMap<Expr, Int>

    companion object Factory {
        private class ResolverStateImpl(override val scopes: Stack<MutableMap<String, Boolean>>, override val locals: HashMap<Expr, Int>) : ResolverState

        fun create(scopes: Stack<MutableMap<String, Boolean>> = Stack(), locals: HashMap<Expr, Int> = HashMap()): ResolverState = ResolverStateImpl(scopes, locals)
    }
}


private fun ResolverState.declare(name: Token) {
    if (scopes.isEmpty()) return //we are global!
    val scope = scopes.peek()
    if (scope.containsKey(name.lexeme))
        throw LoxError.SyntaxError(name, "Variable with this name already declared in this scope.") //TODO ResolverError.
    scope[name.lexeme] = false
}

private fun ResolverState.define(name: Token) {
    if (scopes.isEmpty()) return
    scopes.peek()[name.lexeme] = true
}

private fun ResolverState.beginScope() {
    scopes.push(HashMap())
}

private fun ResolverState.endScope() {
    scopes.pop()
}

private fun ResolverState.resolveLocal(expr: Expr, name: Token) {
    for (i in scopes.size - 1 downTo 0) {
        if (scopes[i].containsKey(name.lexeme)) {
            locals[expr] = scopes.size - 1 - i
            return
        }
    }
    //we are global!
}