import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("var a = \"global a\";\n" +
                "var b = \"global b\";\n" +
                "var c = \"global c\";\n" +
                "{\n" +
                "  var a = \"outer a\";\n" +
                "  var b = \"outer b\";\n" +
                "  {\n" +
                "    var a = \"inner a\";\n" +
                "    print a;\n" +
                "    print b;\n" +
                "    print c;\n" +
                "  }\n" +
                "  print a;\n" +
                "  print b;\n" +
                "  print c;\n" +
                "}\n" +
                "print a;\n" +
                "print b;\n" +
                "print c;")
    }
}