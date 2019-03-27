package mhashim6.klox


/**
 *@author mhashim6 on 08/03/19
 */

object PrettyAST {

    @JvmStatic
    fun main(args: Array<String>) {
        val expression = Expr.Binary(
                Expr.Unary(Token(TokenType.MINUS, "-", null, 1), Expr.Literal(123)),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Grouping(Expr.Literal(45.67)))

        println(prettify(expression))
    }


    private fun prettify(expr: Expr): String = when (expr) {
        is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        is Expr.Grouping -> parenthesize("group", expr.expression)
        is Expr.Literal -> if (expr.value == null) "nil" else expr.value.toString()
        is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
        is Expr.Variable -> parenthesize(expr.name.lexeme)
        is Expr.Assign -> parenthesize(expr.name.lexeme)
        Expr.Empty -> ""
        is Expr.Call -> TODO()
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        return "($name ${exprs.map(PrettyAST::prettify).reduce { acc, expr -> "$acc $expr" }})"
    }
}