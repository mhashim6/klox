import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("class Doughnut {\n" +
                "  cook() {\n" +
                "    print \"Fry until golden brown.\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "class BostonCream < Doughnut {\n" +
                "  cook() {\n" +
                "    super.cook();\n" +
                "    print \"Pipe full of custard and coat with chocolate.\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "BostonCream().cook();\n" +
                "// Prints:\n" +
                "// Fry until golden brown.\n" +
                "// Pipe full of custard and coat with chocolate.")
    }
}

