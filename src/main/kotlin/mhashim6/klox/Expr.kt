package mhashim6.klox


sealed class Expr {
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Grouping(val expression: Expr) : Expr()
    class Literal(val value: Any?) : Expr()
    class Unary(val operator: Token, val right: Expr) : Expr()
    class Variable(val name: Token) : Expr()
    class Assign(val name: Token, val value: Expr) : Expr()
    class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()
    object Empty : Expr()
}
