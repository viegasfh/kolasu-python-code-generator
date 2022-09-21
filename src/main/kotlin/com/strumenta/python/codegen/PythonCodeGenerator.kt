package com.strumenta.python.codegen

import com.strumenta.kolasu.model.Node
import java.io.File
import java.util.regex.Pattern
import kotlin.reflect.KClass

// This file contains the code generation for the Python AST
// This logic could be potentially expressed in a DSL, with multi-platform generators. It would permit
// to have code generators usable from all the StarLaSu platforms
// This could potentially be moved to a separate project in the future

class PythonCodeGenerator {

    private val nodePrinters: MutableMap<KClass<*>, NodePrinter> = HashMap()

    init {
        // modules
        recordPrinter<PyModule> {
            val importFroms: List<PyImportFrom> = it.body.filterIsInstance(PyImportFrom::class.java)
            val imports: List<PyImport> = it.body.filterIsInstance(PyImport::class.java)
            val hasImports = imports.isNotEmpty()
            val statements: List<PyStmt> =
                it.body.filterNot { statement -> statement is PyImportFrom || statement is PyImport }
            val hasStatements = statements.isNotEmpty()
            if (importFroms.isNotEmpty()) {
                printList(importFroms, separator = "\n")
                if (hasImports || hasStatements) {
                    printEmptyLine()
                }
            }
            if (imports.isNotEmpty()) {
                printList(imports, separator = "\n")
                if (hasStatements) {
                    printEmptyLine()
                }
            }
            printList(statements, separator = "\n")
        }
        recordPrinter<PyInteractive> {
            val importFroms: List<PyImportFrom> = it.body.filterIsInstance(PyImportFrom::class.java)
            printList(prefix = "", importFroms, "\n", false, "")
            if (importFroms.isNotEmpty()) {
                println()
            }
            val imports: List<PyImport> = it.body.filterIsInstance(PyImport::class.java)
            printList(prefix = "", imports, "\n", false, "")
            if (imports.isNotEmpty()) {
                println()
            }
            it.body.filterNot { statement -> statement is PyImportFrom || statement is PyImport }
                .forEach { statement -> print(statement) }
        }
        recordPrinter<PyExpression> {
            print(it.body)
        }
        // PyFunctionType
        // statements
        recordPrinter<PyFunctionDef> {
            it.decoratorList.forEach { decorator ->
                print("@")
                println(decorator)
            }
            print("def ${it.name}")
            print(it.args)
            if (it.returns != null) {
                print(" -> ")
                print(it.returns)
            }
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
            println()
        }
        recordPrinter<PyAsyncFunctionDef> {
            print("async def ${it.name}")
            print(it.args)
            println(":")
            indent()
            printList(it.body, separator = "\n")
        }
        recordPrinter<PyClassDef> {
            it.decoratorList.forEach { decorator ->
                print("@")
                println(decorator)
            }
            print("class ${it.name}")
            val arguments: MutableList<PyNode> = mutableListOf<PyNode>().apply {
                addAll(it.bases)
                addAll(it.keywords)
            }
            printList(elements = arguments, prefix = "(", postfix = ")", separator = ", ", printEvenIfEmpty = false)
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
            println()
        }
        recordPrinter<PyReturn> {
            print("return")
            if (it.value != null) {
                print(" ")
                print(it.value)
            }
        }
        recordPrinter<PyDelete> {
            print("del ")
            printList(it.targets, separator = ", ")
        }
        recordPrinter<PyAssign> {
            printList(it.targets, separator = " = ")
            print(" = ")
            print(it.value)
        }
        recordPrinter<PyAugAssign> {
            print(it.target)
            when (it.op) {
                PyOperator.Add -> print(" += ")
                PyOperator.Sub -> print(" -= ")
                PyOperator.Mult -> print(" *= ")
                PyOperator.MatMult -> print(" @= ")
                PyOperator.Div -> print(" /= ")
                PyOperator.Mod -> print(" %= ")
                PyOperator.Pow -> print(" **= ")
                PyOperator.LShift -> print(" <<= ")
                PyOperator.RShift -> print(" >>= ")
                PyOperator.BitOr -> print(" |= ")
                PyOperator.BitXor -> print(" ^= ")
                PyOperator.BitAnd -> print(" &= ")
                PyOperator.FloorDiv -> print(" //= ")
            }
            print(it.value)
        }
        recordPrinter<PyAnnAssign> {
            if (it.simple == 1) {
                print("(")
            }
            print(it.target)
            if (it.simple == 1) {
                print(")")
            }
            print(": ")
            print(it.annotation)
            if (it.value != null) {
                print(" = ")
                print(it.value)
            }
        }
        recordPrinter<PyFor> {
            print("for ")
            print(it.target)
            print(" in ")
            print(it.iter)
            println(":")
            indent()
            printList(elements = it.body, separator = "\n")
            dedent()
            if (it.orElse.isNotEmpty()) {
                println()
                println("else:")
                indent()
                printList(it.orElse, separator = "\n")
                dedent()
            }
        }
        // PyAsyncFor
        recordPrinter<PyWhile> {
            print("while ")
            print(it.test)
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
            if (it.orElse.isNotEmpty()) {
                println()
                println("else:")
                indent()
                printList(it.orElse, separator = "\n")
                dedent()
            }
        }
        recordPrinter<PyIf> {
            print("if ")
            print(it.test)
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
            if (it.orElse.size == 1 && it.orElse[0] is PyIf) {
                println()
                print("el")
                print(it.orElse[0])
            } else if (it.orElse.isNotEmpty()) {
                println()
                println("else:")
                indent()
                printList(it.orElse, separator = "\n")
                dedent()
            }
        }
        recordPrinter<PyWith> {
            print("with ")
            printList(it.items, separator = ", ")
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
        }
        // PyAsyncWith
        recordPrinter<PyMatch> {
            print("match ")
            print(it.subject)
            println(":")
            indent()
            printList(it.cases, separator = "\n")
            dedent()
        }
        recordPrinter<PyRaise> {
            print("raise ")
            print(it.exc)
            print(" from ")
            print(it.cause)
        }
        recordPrinter<PyTry> {
            println("try:")
            indent()
            printList(it.body, separator = "\n")
            dedent()
            if (it.handlers.isNotEmpty()) {
                println()
                printList(it.handlers, separator = "\n")
            }
            if (it.orElse.isNotEmpty()) {
                println()
                println("else:")
                indent()
                printList(it.orElse, separator = "\n")
                dedent()
            }
            if (it.finalBody.isNotEmpty()) {
                println()
                println("finally:")
                indent()
                printList(it.finalBody, separator = "\n")
                dedent()
            }
        }
        // PyTryStar
        recordPrinter<PyAssert> {
            print("assert ")
            print(it.test)
            if (it.msg != null) {
                print(", ")
                print(it.msg)
            }
        }
        recordPrinter<PyImport> {
            printList(
                elements = it.names,
                prefix = "import ",
                postfix = "",
                separator = ", ",
                printEvenIfEmpty = false
            )
        }
        recordPrinter<PyImportFrom> {
            printList(
                elements = it.names,
                prefix = "from ${".".repeat(it.level ?: 0)}${it.module} import ",
                postfix = "",
                separator = ", ",
                printEvenIfEmpty = false
            )
        }
        recordPrinter<PyGlobal> {
            print("global ${it.names.joinToString(", ")}")
        }
        recordPrinter<PyNonLocal> {
            print("nonlocal ${it.names.joinToString(", ")}")
        }
        recordPrinter<PyExprStmt> {
            print(it.value)
        }
        recordPrinter<PyPass> {
            print("pass")
        }
        recordPrinter<PyBreak> {
            print("break")
        }
        recordPrinter<PyContinue> {
            print("continue")
        }
        // expressions
        recordPrinter<PyBoolOpExpr> {
            printList(
                it.values,
                separator = when (it.op) {
                    PyBoolOp.Or -> " or "
                    PyBoolOp.And -> " and "
                }
            )
        }
        recordPrinter<PyNamedExpr> {
            print("(")
            print(it.target)
            print(" := ")
            print(it.value)
            print(")")
        }
        recordPrinter<PyBinOp> {
            print(it.left)
            when (it.op) {
                PyOperator.Add -> print(" + ")
                PyOperator.Sub -> print(" - ")
                PyOperator.Mult -> print(" * ")
                PyOperator.MatMult -> print(" @ ")
                PyOperator.Div -> print(" / ")
                PyOperator.Mod -> print(" % ")
                PyOperator.Pow -> print(" ** ")
                PyOperator.LShift -> print(" << ")
                PyOperator.RShift -> print(" >> ")
                PyOperator.BitOr -> print(" | ")
                PyOperator.BitXor -> print(" ^ ")
                PyOperator.BitAnd -> print(" & ")
                PyOperator.FloorDiv -> print(" // ")
            }
            print(it.right)
        }
        recordPrinter<PyUnaryOpExpr> {
            when (it.op) {
                PyUnaryOp.Invert -> print("~")
                PyUnaryOp.Not -> print("not ")
                PyUnaryOp.UAdd -> print("+")
                PyUnaryOp.USub -> print("-")
            }
            print(it.operand)
        }
        recordPrinter<PyLambda> {
            print("lambda ")
            print(it.args)
            print(": ")
            print(it.body)
        }
        recordPrinter<PyIfExp> {
            print(it.body)
            print(" if ")
            print(it.test)
            print(" else ")
            print(it.orElse)
        }
        recordPrinter<PyDict> {
            print("{")
            it.values.forEachIndexed { index, value ->
                val key = it.keys.getOrNull(index)
                if (key != null) {
                    print(key)
                    print(":")
                } else {
                    print("**")
                }
                print(value)
                if (index < it.values.size - 1) {
                    print(", ")
                }
            }
            print("}")
        }
        recordPrinter<PySet> {
            printList(prefix = "{", it.elts, "}", false, ", ")
        }
        recordPrinter<PyListComp> {
            print("[")
            print(it.elt)
            print(" ")
            printList(it.generators, separator = " ")
            print("]")
        }
        recordPrinter<PySetComp> {
            print("{")
            print(it.elt)
            print(" ")
            printList(it.generators, separator = ", ")
            print("}")
        }
        recordPrinter<PyDictComp> {
            print("{")
            print(it.key)
            print(": ")
            print(it.value)
            print(" ")
            printList(it.generators, separator = ", ")
            print("}")
        }
        recordPrinter<PyGeneratorExp> {
            print("(")
            print(it.elt)
            print(" ")
            printList(it.generators, separator = " ")
            print(")")
        }
        recordPrinter<PyAwait> {
            print("await ")
            print(it.value)
        }
        recordPrinter<PyYield> {
            print("yield")
            if (it.value != null) {
                print(" ")
                print(it.value)
            }
        }
        recordPrinter<PyYieldFrom> {
            print("yield from ")
            print(it.value)
        }
        recordPrinter<PyCompare> {
            print(it.left)
            it.ops.forEachIndexed { index, comparator ->
                when (comparator) {
                    PyCmpOp.Eq -> print(" == ")
                    PyCmpOp.NotEq -> print(" != ")
                    PyCmpOp.Lt -> print(" < ")
                    PyCmpOp.LtE -> print(" <= ")
                    PyCmpOp.Gt -> print(" > ")
                    PyCmpOp.GtE -> print(" >= ")
                    PyCmpOp.Is -> print(" is ")
                    PyCmpOp.IsNot -> print(" is not ")
                    PyCmpOp.In -> print(" in ")
                    PyCmpOp.NotIn -> print(" not in ")
                }
                print(it.comparators[index])
            }
        }
        recordPrinter<PyCall> {
            print(it.func)
            print("(")
            printList(
                prefix = "",
                mutableListOf<PyNode>().apply {
                    addAll(it.args.filter { argument -> argument !is PyStarred })
                    addAll(it.keywords.filter { keyword -> keyword.arg != null })
                    addAll(it.args.filterIsInstance(PyStarred::class.java))
                    addAll(it.keywords.filter { keyword -> keyword.arg == null })
                },
                postfix = "",
                false,
                ", "
            )
            print(")")
        }
        recordPrinter<PyFormattedValue> {
            print("{")
            print(it.value)
            when (it.conversion) {
                115 -> print("!s")
                114 -> print("!r")
                97 -> print("!a")
            }
            if (it.formatSpec != null) {
                print(":")
                print(it.formatSpec)
            }
            print("}")
        }
        recordPrinter<PyJoinedStr> {
            if (it.parent !is PyFormattedValue) {
                print("f\"")
            }
            it.values.forEach { expression -> print(expression) }
            if (it.parent !is PyFormattedValue) {
                print("\"")
            }
        }
        recordPrinter<PyConstantExpr> {
            print(it.value)
        }
        recordPrinter<PyAttribute> {
            print(it.value)
            print(".")
            print(it.attr)
        }
        recordPrinter<PySubscript> {
            print(it.value)
            print("[")
            print(it.slice)
            print("]")
        }
        recordPrinter<PyStarred> {
            print("*")
            print(it.value)
        }
        recordPrinter<PyName> {
            print(it.id)
        }
        recordPrinter<PyList> {
            printList(prefix = "[", it.elts, "]", false, ", ")
        }
        recordPrinter<PyTuple> {
            if (it.parent !is PyAssign && it.parent !is PySubscript) {
                print("(")
            }
            printList(prefix = "", it.elts, "", false, ", ")
            if (it.parent !is PyAssign && it.parent !is PySubscript) {
                print(")")
            }
        }
        recordPrinter<PySlice> {
            printList(listOfNotNull(it.lower, it.upper, it.step), separator = ":")
        }
        recordPrinter<PyComprehension> {
            if (it.isAsync == 1) print("async ")
            print("for ")
            print(it.target)
            print(" in ")
            print(it.iter)
            printList(prefix = " if ", elements = it.ifs, postfix = "", printEvenIfEmpty = false, separator = " if ")
        }
        recordPrinter<PyExceptHandler> {
            print("except ")
            print(it.type)
            if (it.name != null) {
                print(" as ${it.name}")
            }
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
        }
        recordPrinter<PyArguments> {
            val prints: MutableList<() -> Unit> = mutableListOf<() -> Unit>()
            it.posOnlyArgs.forEach { prints.add { print(it) } }
            it.args.forEach { prints.add { print(it) } }
            it.varArg?.let { prints.add { print("*"); print(it) } }
            it.kwOnlyArgs.forEach { prints.add { print(it) } }
            it.kwArg?.let { prints.add { print("**"); print(it) } }
            if (it.parent !is PyLambda) {
                print("(")
            }
            prints.forEachIndexed { index, print ->
                print()
                if (index < prints.size - 1) {
                    print(", ")
                }
            }
            if (it.parent !is PyLambda) {
                print(")")
            }
        }
        recordPrinter<PyArg> {
            print(it.arg)
            if (it.annotation != null) {
                print(": ")
                print(it.annotation)
            }
            if (it.default != null) {
                print("=")
                print(it.default)
            }
        }
        recordPrinter<PyKeyword> {
            if (it.arg != null) {
                print("${it.arg}=")
            } else {
                print("**")
            }
            print(it.value)
        }
        recordPrinter<PyAlias> {
            print(it.name)
            if (it.asName != null) {
                print(" as ${it.asName}")
            }
        }
        recordPrinter<PyWithItem> {
            print(it.contextExpr)
            if (it.optionalVars != null) {
                print(" as ")
                print(it.optionalVars)
            }
        }
        recordPrinter<PyMatchCase> {
            print("case ")
            print(it.pattern)
            if (it.guard != null) {
                print(" if ")
                print(it.guard)
            }
            println(":")
            indent()
            printList(it.body, separator = "\n")
            dedent()
        }
        recordPrinter<PyMatchValue> {
            print(it.value)
        }
        recordPrinter<PyMatchSingleton> {
            print(it.value)
        }
        recordPrinter<PyMatchSequence> {
            printList(elements = it.patterns, prefix = "[", postfix = "]", separator = ", ", printEvenIfEmpty = false)
        }
        recordPrinter<PyMatchMapping> {
            print("{")
            it.keys.forEachIndexed { index, key ->
                print(key)
                print(": ")
                print(it.patterns[index])
                if (index < it.keys.size - 1) {
                    print(", ")
                }
            }
            if (it.rest != null) {
                if (it.keys.isNotEmpty()) {
                    print(", ")
                }
                print("**${it.rest}")
            }
            print("}")
        }
        recordPrinter<PyMatchClass> {
            print(it.cls)
            print("(")
            printList(it.patterns, separator = ", ")
            it.kwdPatterns.forEachIndexed { index, pattern ->
                print(it.kwdAttrs[index])
                print("=")
                print(pattern)
                if (index < it.kwdPatterns.size - 1) {
                    print(", ")
                }
            }
            print(")")
        }
        recordPrinter<PyMatchStar> {
            print("*${it.name ?: "_"}")
        }
        recordPrinter<PyMatchAs> {
            if (it.parent is PyMatchOr) {
                print("(")
            }
            if (it.pattern != null) {
                print(it.pattern)
                print(" as ")
            }
            print(it.name ?: "_")
            if (it.parent is PyMatchOr) {
                print(")")
            }
        }
        recordPrinter<PyMatchOr> {
            printList(it.patterns, separator = " | ")
        }
    }

    private inline fun <reified N1 : Node> recordPrinter(crossinline generation: PrinterOutput.(ast: N1) -> Unit) {
        this.nodePrinters[N1::class] =
            NodePrinter { output: PrinterOutput, node: Node -> output.generation(node as N1) }
    }

    fun generateToString(root: PyNode): String =
        PrinterOutput(this.nodePrinters)
            .apply { this.print(root) }
            .text()

    fun generateToFile(root: PyNode, file: File) =
        file.writeText(this.generateToString(root))
}

val KEYWORDS = listOf(
    "False",
    "None",
    "True",
    "and",
    "as",
    "assert",
    "async",
    "await",
    "break",
    "class",
    "continue",
    "def",
    "del",
    "elif",
    "else",
    "except",
    "finally",
    "for",
    "from",
    "global",
    "if",
    "import",
    "in",
    "is",
    "lambda",
    "nonlocal",
    "not",
    "or",
    "pass",
    "raise",
    "return",
    "try",
    "while",
    "with",
    "yield"
)
val WHITESPACE: Pattern = Pattern.compile("\\s+")
val CAMELCASE: Pattern = Pattern.compile("(?<=.)(?=\\p{Upper}[a-z0-9])")!!

fun String.toPythonPackageName(): String =
    WHITESPACE.split(this.trim()).joinToString(separator = "_") {
        it.replace(CAMELCASE.toRegex(), "_").lowercase()
    }.replace('-', '_').let { if (KEYWORDS.contains(it)) "${it}_" else it }

fun String.toPythonClassName(): String =
    this.replaceWhitespacesWithUnderscores()
        .makeSegmentsCamelCased()
        .removeAllUnderscores()

fun String.toPythonPropertyName(): String =
    this.replaceWhitespacesWithUnderscores()
        .makeSegmentsLowerCased()

fun String.toPythonEnumLiteral(): String =
    WHITESPACE.split(this.trim()).joinToString(separator = "_") {
        it.uppercase()
    }.let { if (KEYWORDS.contains(it)) "${it}_" else it }

private fun String.replaceWhitespacesWithUnderscores(): String =
    WHITESPACE.split(this.trim()).joinToString(separator = "_")

private fun String.makeSegmentsCamelCased(): String =
    this.split("_").joinToString(separator = "_") {
        CAMELCASE.split(it).joinToString(separator = "") { inner ->
            inner.lowercase().replaceFirstChar { character -> character.uppercase() }
        }
    }

private fun String.makeSegmentsLowerCased(): String =
    this.split("_").joinToString(separator = "_") {
        CAMELCASE.split(it).joinToString(separator = "_") { it.lowercase() }
    }

private fun String.removeAllUnderscores(): String =
    this.replace("_", "")
