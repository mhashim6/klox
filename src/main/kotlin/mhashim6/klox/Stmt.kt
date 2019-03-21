package mhashim6.klox


sealed class Stmt {
    class Expression(val expression: Expr) : Stmt()
    class Print(val expression: Expr) : Stmt()
    class Var(val name: Token, val initializer: Expr?) : Stmt()
    class Block(val statements: List<Stmt>) : Stmt()
    class IfStmt(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt) : Stmt()
    class WhileStmt(val condition: Expr, val body: Stmt) : Stmt()
    object Empty : Stmt()
}
