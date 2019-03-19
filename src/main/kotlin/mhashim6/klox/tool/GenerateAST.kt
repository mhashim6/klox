package mhashim6.klox.tool

import java.io.PrintWriter
import java.util.Arrays

/**
 * @author mhashim6 on 08/03/19
 */

object GenerateAST {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory~")
            System.exit(1)
        }
        val outputDir = args[0]
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   ~ left: Expr, operator: Token , right: Expr ",
                "Grouping ~ expression: Expr",
                "Literal  ~ value: Any?",
                "Unary    ~ operator: Token, right: Expr",
                "Variable ~ name: Token",
                "Assign   ~ name: Token, value:Expr"
        ))

        defineAst(outputDir, "Stmt", listOf(
                "Expression ~ expression: Expr",
                "Print ~ expression: Expr",
                "Var ~ name: Token, initializer: Expr?",
                "Block ~ statements: List<Stmt>",
                "IfStmt ~ condition:Expr, thenBranch: Stmt, elseBranch: Stmt?",
                "Empty ~ "
        ))
    }

    private fun defineAst(
            outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println("package mhashim6.klox")
        writer.println()
//        writer.println("import java.util.List")
        writer.println()
        writer.println("sealed class $baseName {")

        // The AST classes.
        for (type in types) {
            val className = type.split("~")[0].trim()
            val fields = type.split("~")[1].trim().split(",")
            defineType(writer, baseName, className, fields)
        }

        writer.println("}")
        writer.close()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fields: List<String>) {
        writer.println("    class " + className
                + "(${fields.map { "val $it" }.reduce { acc, field -> "$acc, $field" }})"
                + " : " + baseName + " ()")
    }
}