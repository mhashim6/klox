import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.runSource("var a = 1;\n" +
                "var b = 2;\n" +
                "a = 3;\n" +
                "print a + b;\n" +
                "c = 16;")
    }
}