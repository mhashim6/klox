import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("class Cake {\n" +
                "  taste() {\n" +
                "    var adjective = \"delicious\";\n" +
                "    print \"The \" + this.flavor + \" cake is \" + adjective + \"!\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var cake = Cake();\n" +
                "cake.flavor = \"German chocolate\";\n" +
                "cake.taste(); // Prints \"The German chocolate cake is delicious!\".." +
                "\nprint this;")
    }
}

