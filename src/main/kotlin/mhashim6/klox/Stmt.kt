package mhashim6.klox


sealed class Stmt {
    class Expression(val expression: Expr) : Stmt()
    class Print(val expression: Expr) : Stmt()
    class Class(val name: Token, val methods: List<Fun>) : Stmt()
    class Var(val name: Token, val initializer: Expr) : Stmt()
    class Fun(val name: Token, val parameters: List<Token>, val body: Stmt) : Stmt()
    class Return(val keyword: Token, val value: Expr?) : Stmt()
    class Block(val statements: List<Stmt>) : Stmt()
    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt) : Stmt()
    class While(val condition: Expr, val body: Stmt) : Stmt()
    class Break(val keyword: Token) : Stmt()
    object Empty : Stmt()
}
