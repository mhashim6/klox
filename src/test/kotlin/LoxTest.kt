import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("var x = 5;\n" +
                "while(x < 17){\n" +
                "    x = x + 1;\n" +
                "    print x;\n" +
                "}")
    }
}

