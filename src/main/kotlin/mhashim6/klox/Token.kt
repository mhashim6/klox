package mhashim6.klox

/**
 * @author mhashim6 on 08/03/19
 */
class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {

    override fun toString(): String {
        return type.toString() + " " + lexeme + " " + literal
    }
}
