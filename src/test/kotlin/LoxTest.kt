import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("fun testBreak() = for(var i = 0; i < 25; i = i+1){\n" +
                "    if(i >= 15) break;\n" +
                "    else print i;\n" +
                "}\n testBreak();")
    }
}


