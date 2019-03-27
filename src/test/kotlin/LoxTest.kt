import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("var a = \"global\";\n" +
                "{\n" +
                "  fun showA() {\n" +
                "    print a;\n" +
                "  }\n" +
                "\n" +
                "  showA();\n" +
                "  var a = \"block\";\n" +
                "  showA();\n" +
                "}")
    }
}

