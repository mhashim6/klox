import mhashim6.klox.Lox
import org.junit.Test

/**
 * @author mhashim6 on 08/03/19
 */
class LoxTest {

    @Test
    fun run() {
        Lox.run("if(5 == 5){print 5/0;} else{print \"dude, you've got issues.\";}")
    }
}