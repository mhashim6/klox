package mhashim6.klox

/**
 *@author mhashim6 on 09/03/19
 */
class RuntimeError(val token: Token, override val message: String) : RuntimeException(message) {
}