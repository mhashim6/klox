import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("fun fibonacci(n) {\n" +
                "  if (n <= 1) return n;\n" +
                "  return fibonacci(n - 2) + fibonacci(n - 1);\n" +
                "}\n" +
                "\n" +
                "for (var i = 0; i < 20; i = i + 1) {\n" +
                "  print fibonacci(i);\n" +
                "}")
    }
}

