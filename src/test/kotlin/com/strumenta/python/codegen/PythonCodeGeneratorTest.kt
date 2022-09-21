package com.strumenta.python.codegen

import com.strumenta.kolasu.model.assignParents
import org.junit.Test
import kotlin.test.assertEquals

class PythonCodeGeneratorTest {

    private val codeGenerator: PythonCodeGenerator = PythonCodeGenerator()

    @Test
    fun printToStringPyConstant() {
        val ast = PyExpression(body = PyConstantExpr(value = "123")).apply { assignParents() }
        assertEquals("123", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyJoinedStr() {
        val ast = PyExpression(
            body = PyJoinedStr(
                values = mutableListOf(
                    PyConstantExpr(value = "sin("),
                    PyFormattedValue(
                        value = PyName(id = "a", ctx = PyExprContext.Load),
                        conversion = -1
                    ),
                    PyConstantExpr(value = ") is "),
                    PyFormattedValue(
                        value = PyCall(
                            func = PyName(id = "sin", ctx = PyExprContext.Load),
                            args = mutableListOf(
                                PyName(id = "a", ctx = PyExprContext.Load)
                            ),
                            keywords = mutableListOf()
                        ),
                        conversion = -1,
                        formatSpec = PyJoinedStr(
                            values = mutableListOf(
                                PyConstantExpr(value = ".3")
                            )
                        )
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("f\"sin({a}) is {sin(a):.3}\"", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyList() {
        val ast = PyExpression(
            body = PyList(
                elts = mutableListOf(
                    PyConstantExpr(value = "1"),
                    PyConstantExpr(value = "2"),
                    PyConstantExpr(value = "3")
                ),
                ctx = PyExprContext.Load
            )
        ).apply { assignParents() }
        assertEquals("[1, 2, 3]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyTuple() {
        val ast = PyExpression(
            body = PyTuple(
                elts = mutableListOf(
                    PyConstantExpr(value = "1"),
                    PyConstantExpr(value = "2"),
                    PyConstantExpr(value = "3")
                ),
                ctx = PyExprContext.Load
            )
        ).apply { assignParents() }
        assertEquals("(1, 2, 3)", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPySet() {
        val ast = PyExpression(
            body = PySet(
                elts = mutableListOf(
                    PyConstantExpr(value = "1"),
                    PyConstantExpr(value = "2"),
                    PyConstantExpr(value = "3")
                )
            )
        ).apply { assignParents() }
        assertEquals("{1, 2, 3}", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyDict() {
        val ast = PyExpression(
            body = PyDict(
                keys = mutableListOf(
                    PyConstantExpr(value = "\"a\"")
                ),
                values = mutableListOf(
                    PyConstantExpr(value = "1"),
                    PyName(id = "d", ctx = PyExprContext.Load)
                )
            )
        ).apply { assignParents() }
        assertEquals("{\"a\":1, **d}", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyNameLoad() {
        val ast = PyModule(
            body = mutableListOf(
                PyExprStmt(
                    value = PyName(id = "a", ctx = PyExprContext.Load)
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyNameStore() {
        val ast = PyModule(
            body = mutableListOf(
                PyAssign(
                    targets = mutableListOf(
                        PyName(id = "a", ctx = PyExprContext.Store)
                    ),
                    value = PyConstantExpr(value = "1")
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a = 1", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyNameDel() {
        val ast = PyModule(
            body = mutableListOf(
                PyDelete(
                    targets = mutableListOf(
                        PyName(id = "a", ctx = PyExprContext.Del)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("del a", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyStarred() {
        val ast = PyModule(
            body = mutableListOf(
                PyAssign(
                    targets = mutableListOf(
                        PyTuple(
                            elts = mutableListOf(
                                PyName(id = "a", ctx = PyExprContext.Store),
                                PyStarred(
                                    value = PyName(id = "b", ctx = PyExprContext.Store),
                                    ctx = PyExprContext.Store
                                )
                            ),
                            ctx = PyExprContext.Store
                        ),
                    ),
                    value = PyName(id = "it", ctx = PyExprContext.Load)
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a, *b = it", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyUnaryOpExprUSub() {
        val ast = PyModule(
            body = mutableListOf(
                PyExprStmt(
                    value = PyUnaryOpExpr(
                        op = PyUnaryOp.USub,
                        operand = PyName(id = "a", ctx = PyExprContext.Load)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("-a", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyUnaryOpExprNot() {
        val ast = PyExpression(
            body = PyUnaryOpExpr(
                op = PyUnaryOp.Not,
                operand = PyName(id = "x", ctx = PyExprContext.Load)
            )
        ).apply { assignParents() }
        assertEquals("not x", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyBinOpAdd() {
        val ast = PyExpression(
            body = PyBinOp(
                left = PyName(id = "x", ctx = PyExprContext.Load),
                op = PyOperator.Add,
                right = PyName(id = "y", ctx = PyExprContext.Load)
            )
        ).apply { assignParents() }
        assertEquals("x + y", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyBoolOpOr() {
        val ast = PyExpression(
            body = PyBoolOpExpr(
                op = PyBoolOp.Or,
                values = mutableListOf(
                    PyName(id = "x", ctx = PyExprContext.Load),
                    PyName(id = "y", ctx = PyExprContext.Load)
                )
            )
        ).apply { assignParents() }
        assertEquals("x or y", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyCompare() {
        val ast = PyExpression(
            body = PyCompare(
                left = PyConstantExpr(value = "1"),
                ops = mutableListOf(
                    PyCmpOp.LtE,
                    PyCmpOp.Lt
                ),
                comparators = mutableListOf(
                    PyName(id = "a", ctx = PyExprContext.Load),
                    PyConstantExpr(value = "10")
                )
            )
        ).apply { assignParents() }
        assertEquals("1 <= a < 10", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyCall() {
        val ast = PyExpression(
            body = PyCall(
                func = PyName(id = "func", ctx = PyExprContext.Load),
                args = mutableListOf(
                    PyName(id = "a", ctx = PyExprContext.Load),
                    PyStarred(
                        value = PyName(id = "d", ctx = PyExprContext.Load),
                        ctx = PyExprContext.Load
                    )
                ),
                keywords = mutableListOf(
                    PyKeyword(
                        arg = "b",
                        value = PyName(id = "c", ctx = PyExprContext.Load)
                    ),
                    PyKeyword(
                        value = PyName(id = "e", ctx = PyExprContext.Load)
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("func(a, b=c, *d, **e)", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringIfExp() {
        val ast = PyExpression(
            body = PyIfExp(
                test = PyName(id = "b", ctx = PyExprContext.Load),
                body = PyName(id = "a", ctx = PyExprContext.Load),
                orElse = PyName(id = "c", ctx = PyExprContext.Load),
            )
        ).apply { assignParents() }
        assertEquals("a if b else c", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAttribute() {
        val ast = PyExpression(
            body = PyAttribute(
                value = PyName(id = "snake", ctx = PyExprContext.Load),
                attr = "colour",
                ctx = PyExprContext.Load
            )
        ).apply { assignParents() }
        assertEquals("snake.colour", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyNamedExpr() {
        val ast = PyExpression(
            body = PyNamedExpr(
                target = PyName(id = "x", ctx = PyExprContext.Store),
                value = PyConstantExpr(value = "4")
            )
        ).apply { assignParents() }
        assertEquals("(x := 4)", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPySubscript() {
        val ast = PyExpression(
            body = PySubscript(
                value = PyName(id = "l", ctx = PyExprContext.Load),
                slice = PyTuple(
                    elts = mutableListOf(
                        PySlice(
                            lower = PyConstantExpr(value = "1"),
                            upper = PyConstantExpr(value = "2")
                        ),
                        PyConstantExpr(value = "3")
                    ),
                    ctx = PyExprContext.Load
                ),
                ctx = PyExprContext.Load
            )
        ).apply { assignParents() }
        assertEquals("l[1:2, 3]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPySlice() {
        val ast = PyExpression(
            body = PySubscript(
                value = PyName(id = "l", ctx = PyExprContext.Load),
                slice = PySlice(
                    lower = PyConstantExpr(value = "1"),
                    upper = PyConstantExpr(value = "2")
                ),
                ctx = PyExprContext.Load
            )
        ).apply { assignParents() }
        assertEquals("l[1:2]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyListComp() {
        val ast = PyExpression(
            body = PyListComp(
                elt = PyName(id = "x", ctx = PyExprContext.Load),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "x", ctx = PyExprContext.Store),
                        iter = PyName(id = "numbers", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 0
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("[x for x in numbers]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyDictComp() {
        val ast = PyExpression(
            body = PyDictComp(
                key = PyName(id = "x", ctx = PyExprContext.Load),
                value = PyBinOp(
                    left = PyName(id = "x", ctx = PyExprContext.Load),
                    op = PyOperator.Pow,
                    right = PyConstantExpr(value = "2")
                ),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "x", ctx = PyExprContext.Store),
                        iter = PyName(id = "numbers", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 0
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("{x: x ** 2 for x in numbers}", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPySetComp() {
        val ast = PyExpression(
            body = PySetComp(
                elt = PyName(id = "x", ctx = PyExprContext.Load),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "x", ctx = PyExprContext.Store),
                        iter = PyName(id = "numbers", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 0
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("{x for x in numbers}", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyComprehension() {
        val ast = PyExpression(
            body = PyListComp(
                elt = PyCall(
                    func = PyName(id = "ord", ctx = PyExprContext.Load),
                    args = mutableListOf(
                        PyName(id = "c", ctx = PyExprContext.Load)
                    ),
                    keywords = mutableListOf()
                ),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "line", ctx = PyExprContext.Store),
                        iter = PyName(id = "file", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 0
                    ),
                    PyComprehension(
                        target = PyName(id = "c", ctx = PyExprContext.Store),
                        iter = PyName(id = "line", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 0
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("[ord(c) for line in file for c in line]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyGeneratorExp() {
        val ast = PyExpression(
            body = PyGeneratorExp(
                elt = PyBinOp(
                    left = PyName(id = "n", ctx = PyExprContext.Load),
                    op = PyOperator.Pow,
                    right = PyConstantExpr(value = "2")
                ),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "n", ctx = PyExprContext.Store),
                        iter = PyName(id = "it", ctx = PyExprContext.Load),
                        ifs = mutableListOf(
                            PyCompare(
                                left = PyName(id = "n", ctx = PyExprContext.Load),
                                ops = mutableListOf(
                                    PyCmpOp.Gt
                                ),
                                comparators = mutableListOf(
                                    PyConstantExpr(value = "5")
                                )
                            ),
                            PyCompare(
                                left = PyName(id = "n", ctx = PyExprContext.Load),
                                ops = mutableListOf(
                                    PyCmpOp.Lt
                                ),
                                comparators = mutableListOf(
                                    PyConstantExpr(value = "10")
                                )
                            )
                        ),
                        isAsync = 0
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("(n ** 2 for n in it if n > 5 if n < 10)", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAsyncListComp() {
        val ast = PyExpression(
            body = PyListComp(
                elt = PyName(id = "i", ctx = PyExprContext.Load),
                generators = mutableListOf(
                    PyComprehension(
                        target = PyName(id = "i", ctx = PyExprContext.Store),
                        iter = PyName(id = "soc", ctx = PyExprContext.Load),
                        ifs = mutableListOf(),
                        isAsync = 1
                    )
                )
            )
        ).apply { assignParents() }
        assertEquals("[i async for i in soc]", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAssignMultiple() {
        val ast = PyModule(
            body = mutableListOf(
                PyAssign(
                    targets = mutableListOf(
                        PyName(id = "a", ctx = PyExprContext.Load),
                        PyName(id = "b", ctx = PyExprContext.Load)
                    ),
                    value = PyConstantExpr(value = "1")
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a = b = 1", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAssignUnpacking() {
        val ast = PyModule(
            body = mutableListOf(
                PyAssign(
                    targets = mutableListOf(
                        PyTuple(
                            elts = mutableListOf(
                                PyName(id = "a", ctx = PyExprContext.Store),
                                PyName(id = "b", ctx = PyExprContext.Store)
                            ),
                            ctx = PyExprContext.Store
                        )
                    ),
                    value = PyName(id = "c", ctx = PyExprContext.Load)
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a, b = c", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAnnAssign() {
        val ast = PyModule(
            body = mutableListOf(
                PyAnnAssign(
                    target = PyName(id = "c", ctx = PyExprContext.Store),
                    annotation = PyName(id = "int", ctx = PyExprContext.Load),
                    simple = 0
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("c: int", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAnnAssignWithParenthesis() {
        val ast = PyModule(
            body = mutableListOf(
                PyAnnAssign(
                    target = PyName(id = "a", ctx = PyExprContext.Store),
                    annotation = PyName(id = "int", ctx = PyExprContext.Load),
                    value = PyConstantExpr(value = "1"),
                    simple = 1
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("(a): int = 1", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAnnAssignWithAttributeAnnotation() {
        val ast = PyModule(
            body = mutableListOf(
                PyAnnAssign(
                    target = PyAttribute(
                        value = PyName(id = "a", ctx = PyExprContext.Load),
                        attr = "b",
                        ctx = PyExprContext.Store
                    ),
                    annotation = PyName(id = "int", ctx = PyExprContext.Load),
                    simple = 0
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a.b: int", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAnnAssignWithSubscriptAnnotation() {
        val ast = PyModule(
            body = mutableListOf(
                PyAnnAssign(
                    target = PySubscript(
                        value = PyName(id = "a", ctx = PyExprContext.Load),
                        slice = PyConstantExpr(value = "1"),
                        ctx = PyExprContext.Store
                    ),
                    annotation = PyName(id = "int", ctx = PyExprContext.Load),
                    simple = 0
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("a[1]: int", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAugAssign() {
        val ast = PyModule(
            body = mutableListOf(
                PyAugAssign(
                    target = PyName(id = "x", ctx = PyExprContext.Store),
                    op = PyOperator.Add,
                    value = PyConstantExpr(value = "2")
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("x += 2", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyRaise() {
        val ast = PyModule(
            body = mutableListOf(
                PyRaise(
                    exc = PyName(id = "x", ctx = PyExprContext.Load),
                    cause = PyName(id = "y", ctx = PyExprContext.Load)
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("raise x from y", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAssert() {
        val ast = PyModule(
            body = mutableListOf(
                PyAssert(
                    test = PyName(id = "x", ctx = PyExprContext.Load),
                    msg = PyName(id = "y", ctx = PyExprContext.Load)
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("assert x, y", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyDelete() {
        val ast = PyModule(
            body = mutableListOf(
                PyDelete(
                    targets = mutableListOf(
                        PyName(id = "x", ctx = PyExprContext.Del),
                        PyName(id = "y", ctx = PyExprContext.Del),
                        PyName(id = "z", ctx = PyExprContext.Del)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("del x, y, z", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyPass() {
        val ast = PyModule(
            body = mutableListOf(
                PyPass()
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("pass", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyImport() {
        val ast = PyModule(
            body = mutableListOf(
                PyImport(
                    names = mutableListOf(
                        PyAlias(name = "x"),
                        PyAlias(name = "y"),
                        PyAlias(name = "z")
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("import x, y, z", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyImportFrom() {
        val ast = PyModule(
            body = mutableListOf(
                PyImportFrom(
                    module = "y",
                    names = mutableListOf(
                        PyAlias(name = "x"),
                        PyAlias(name = "y"),
                        PyAlias(name = "z")
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("from y import x, y, z", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyAlias() {
        val ast = PyModule(
            body = mutableListOf(
                PyImportFrom(
                    module = "foo.bar",
                    names = mutableListOf(
                        PyAlias(name = "a", asName = "b"),
                        PyAlias(name = "c")
                    ),
                    level = 2
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("from ..foo.bar import a as b, c", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyIf() {
        val ast = PyModule(
            body = mutableListOf(
                PyIf(
                    test = PyName(id = "x", ctx = PyExprContext.Load),
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    ),
                    orElse = mutableListOf(
                        PyIf(
                            test = PyName(id = "y", ctx = PyExprContext.Load),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            ),
                            orElse = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            if x:
                ...
            elif y:
                ...
            else:
                ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyFor() {
        val ast = PyModule(
            body = mutableListOf(
                PyFor(
                    target = PyName(id = "x", ctx = PyExprContext.Store),
                    iter = PyName(id = "y", ctx = PyExprContext.Load),
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    ),
                    orElse = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            for x in y:
                ...
            else:
                ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyWhile() {
        val ast = PyModule(
            body = mutableListOf(
                PyWhile(
                    test = PyName(id = "x", ctx = PyExprContext.Load),
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    ),
                    orElse = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            while x:
                ...
            else:
                ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyBreakAndContinue() {
        val ast = PyModule(
            body = mutableListOf(
                PyFor(
                    target = PyName(id = "a", ctx = PyExprContext.Store),
                    iter = PyName(id = "b", ctx = PyExprContext.Load),
                    body = mutableListOf(
                        PyIf(
                            test = PyCompare(
                                left = PyName(id = "a", ctx = PyExprContext.Load),
                                ops = mutableListOf(
                                    PyCmpOp.Gt
                                ),
                                comparators = mutableListOf(
                                    PyConstantExpr(value = "5")
                                )
                            ),
                            body = mutableListOf(
                                PyBreak()
                            ),
                            orElse = mutableListOf(
                                PyContinue()
                            )
                        )
                    ),
                    orElse = mutableListOf()
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            for a in b:
                if a > 5:
                    break
                else:
                    continue
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyTry() {
        val ast = PyModule(
            body = mutableListOf(
                PyTry(
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    ),
                    handlers = mutableListOf(
                        PyExceptHandler(
                            type = PyName(id = "Exception", ctx = PyExprContext.Load),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        ),
                        PyExceptHandler(
                            type = PyName(id = "OtherException", ctx = PyExprContext.Load),
                            name = "e",
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    ),
                    orElse = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    ),
                    finalBody = mutableListOf(
                        PyExprStmt(
                            value = PyConstantExpr(value = "...")
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            try:
                ...
            except Exception:
                ...
            except OtherException as e:
                ...
            else:
                ...
            finally:
                ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyExceptHandler() {
        val ast = PyModule(
            body = mutableListOf(
                PyTry(
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyBinOp(
                                left = PyName(id = "a", ctx = PyExprContext.Load),
                                op = PyOperator.Add,
                                right = PyConstantExpr(value = "1")
                            )
                        )
                    ),
                    handlers = mutableListOf(
                        PyExceptHandler(
                            type = PyName(id = "TypeError", ctx = PyExprContext.Load),
                            body = mutableListOf(
                                PyPass()
                            )
                        )
                    ),
                    orElse = mutableListOf(),
                    finalBody = mutableListOf()
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            try:
                a + 1
            except TypeError:
                pass
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyWithItem() {
        val ast = PyModule(
            body = mutableListOf(
                PyWith(
                    items = mutableListOf(
                        PyWithItem(
                            contextExpr = PyName(id = "a", ctx = PyExprContext.Load),
                            optionalVars = PyName(id = "b", ctx = PyExprContext.Store)
                        ),
                        PyWithItem(
                            contextExpr = PyName(id = "c", ctx = PyExprContext.Load),
                            optionalVars = PyName(id = "d", ctx = PyExprContext.Store)
                        )
                    ),
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyCall(
                                func = PyName(id = "something", ctx = PyExprContext.Load),
                                args = mutableListOf(
                                    PyName(id = "b", ctx = PyExprContext.Load),
                                    PyName(id = "d", ctx = PyExprContext.Load)
                                ),
                                keywords = mutableListOf()
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            with a as b, c as d:
                something(b, d)
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchCase() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchSequence(
                                patterns = mutableListOf(
                                    PyMatchAs(name = "x")
                                )
                            ),
                            guard = PyCompare(
                                left = PyName(id = "x", ctx = PyExprContext.Load),
                                ops = mutableListOf(
                                    PyCmpOp.Gt
                                ),
                                comparators = mutableListOf(
                                    PyConstantExpr(value = "0")
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        ),
                        PyMatchCase(
                            pattern = PyMatchClass(
                                cls = PyName(id = "tuple", ctx = PyExprContext.Load),
                                patterns = mutableListOf(),
                                kwdAttrs = mutableListOf(),
                                kwdPatterns = mutableListOf()
                            ),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case [x] if x > 0:
                    ...
                case tuple():
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchValue() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchValue(
                                value = PyConstantExpr(value = "\"Relevant\"")
                            ),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case "Relevant":
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchSingleton() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchSingleton(value = "None"),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case None:
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchSequence() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchSequence(
                                patterns = mutableListOf(
                                    PyMatchValue(
                                        value = PyConstantExpr(value = "1")
                                    ),
                                    PyMatchValue(
                                        value = PyConstantExpr(value = "2")
                                    )
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(
                                    value = PyConstantExpr(value = "...")
                                )
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case [1, 2]:
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchStar() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchSequence(
                                patterns = mutableListOf(
                                    PyMatchValue(value = PyConstantExpr(value = "1")),
                                    PyMatchValue(value = PyConstantExpr(value = "2")),
                                    PyMatchStar(name = "rest")
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        ),
                        PyMatchCase(
                            pattern = PyMatchSequence(
                                patterns = mutableListOf(
                                    PyMatchStar()
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case [1, 2, *rest]:
                    ...
                case [*_]:
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchMapping() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchMapping(
                                keys = mutableListOf(
                                    PyConstantExpr(value = "1"),
                                    PyConstantExpr(value = "2")
                                ),
                                patterns = mutableListOf(
                                    PyMatchAs(),
                                    PyMatchAs()
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        ),
                        PyMatchCase(
                            pattern = PyMatchMapping(
                                keys = mutableListOf(),
                                patterns = mutableListOf(),
                                rest = "rest"
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case {1: _, 2: _}:
                    ...
                case {**rest}:
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchClass() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchClass(
                                cls = PyName(id = "Point2D", ctx = PyExprContext.Load),
                                patterns = mutableListOf(
                                    PyMatchValue(value = PyConstantExpr(value = "0")),
                                    PyMatchValue(value = PyConstantExpr(value = "0"))
                                ),
                                kwdAttrs = mutableListOf(),
                                kwdPatterns = mutableListOf()
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        ),
                        PyMatchCase(
                            pattern = PyMatchClass(
                                cls = PyName(id = "Point3D", ctx = PyExprContext.Load),
                                patterns = mutableListOf(),
                                kwdAttrs = mutableListOf("x", "y", "z"),
                                kwdPatterns = mutableListOf(
                                    PyMatchValue(value = PyConstantExpr(value = "0")),
                                    PyMatchValue(value = PyConstantExpr(value = "0")),
                                    PyMatchValue(value = PyConstantExpr(value = "0"))
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case Point2D(0, 0):
                    ...
                case Point3D(x=0, y=0, z=0):
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchAs() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchAs(
                                pattern = PyMatchSequence(
                                    patterns = mutableListOf(
                                        PyMatchAs(name = "x")
                                    )
                                ),
                                name = "y"
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        ),
                        PyMatchCase(
                            pattern = PyMatchAs(),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case [x] as y:
                    ...
                case _:
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyMatchOr() {
        val ast = PyModule(
            body = mutableListOf(
                PyMatch(
                    subject = PyName(id = "x", ctx = PyExprContext.Load),
                    cases = mutableListOf(
                        PyMatchCase(
                            pattern = PyMatchOr(
                                patterns = mutableListOf(
                                    PyMatchSequence(
                                        patterns = mutableListOf(
                                            PyMatchAs(name = "x")
                                        )
                                    ),
                                    PyMatchAs(name = "y")
                                )
                            ),
                            body = mutableListOf(
                                PyExprStmt(value = PyConstantExpr(value = "..."))
                            )
                        )
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            match x:
                case [x] | (y):
                    ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyLambda() {
        val ast = PyModule(
            body = mutableListOf(
                PyExprStmt(
                    value = PyLambda(
                        args = PyArguments(
                            posOnlyArgs = mutableListOf(),
                            args = mutableListOf(
                                PyArg(arg = "x"),
                                PyArg(arg = "y")
                            ),
                            kwOnlyArgs = mutableListOf()
                        ),
                        body = PyConstantExpr(value = "...")
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            lambda x, y: ...
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyArg() {
        val ast = PyModule(
            body = mutableListOf(
                PyFunctionDef(
                    name = "f",
                    args = PyArguments(
                        posOnlyArgs = mutableListOf(),
                        args = mutableListOf(
                            PyArg(
                                arg = "a",
                                annotation = PyConstantExpr(value = "'annotation'")
                            ),
                            PyArg(
                                arg = "b",
                                default = PyConstantExpr(value = "1")
                            ),
                            PyArg(
                                arg = "c",
                                default = PyConstantExpr(value = "2")
                            )
                        ),
                        varArg = PyArg(arg = "d"),
                        kwOnlyArgs = mutableListOf(
                            PyArg(arg = "e"),
                            PyArg(arg = "f", default = PyConstantExpr(value = "3"))
                        ),
                        kwArg = PyArg(arg = "g")
                    ),
                    body = mutableListOf(
                        PyPass()
                    ),
                    decoratorList = mutableListOf(
                        PyName(id = "decorator1", ctx = PyExprContext.Load),
                        PyName(id = "decorator2", ctx = PyExprContext.Load)
                    ),
                    returns = PyConstantExpr(value = "'return annotation'")
                ),
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            @decorator1
            @decorator2
            def f(a: 'annotation', b=1, c=2, *d, e, f=3, **g) -> 'return annotation':
                pass
            
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyReturn() {
        val ast = PyModule(
            body = mutableListOf(
                PyReturn(value = PyConstantExpr(value = "4"))
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("return 4".trimIndent(), codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyYield() {
        val ast = PyModule(
            body = mutableListOf(
                PyExprStmt(
                    value = PyYield(
                        value = PyName(id = "x", ctx = PyExprContext.Load)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("yield x", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyYieldFrom() {
        val ast = PyModule(
            body = mutableListOf(
                PyExprStmt(
                    value = PyYieldFrom(
                        value = PyName(id = "x", ctx = PyExprContext.Load)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("yield from x", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyGlobal() {
        val ast = PyModule(
            body = mutableListOf(
                PyGlobal(names = mutableListOf("x", "y", "z"))
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("global x, y, z", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyNonLocal() {
        val ast = PyModule(
            body = mutableListOf(
                PyNonLocal(names = mutableListOf("x", "y", "z"))
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals("nonlocal x, y, z", codeGenerator.generateToString(ast))
    }

    @Test
    fun printToStringPyClassDef() {
        val ast = PyModule(
            body = mutableListOf(
                PyClassDef(
                    name = "Foo",
                    bases = mutableListOf(
                        PyName(id = "base1", ctx = PyExprContext.Load),
                        PyName(id = "base2", ctx = PyExprContext.Load)
                    ),
                    keywords = mutableListOf(
                        PyKeyword(
                            arg = "metaclass",
                            value = PyName(id = "meta", ctx = PyExprContext.Load)
                        )
                    ),
                    body = mutableListOf(
                        PyPass()
                    ),
                    decoratorList = mutableListOf(
                        PyName(id = "decorator1", ctx = PyExprContext.Load),
                        PyName(id = "decorator2", ctx = PyExprContext.Load)
                    )
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            @decorator1
            @decorator2
            class Foo(base1, base2, metaclass=meta):
                pass
            
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }

    @Test
    fun printToStringPyAwait() {
        val ast = PyModule(
            body = mutableListOf(
                PyAsyncFunctionDef(
                    name = "f",
                    args = PyArguments(
                        posOnlyArgs = mutableListOf(),
                        args = mutableListOf(),
                        kwOnlyArgs = mutableListOf(),
                    ),
                    body = mutableListOf(
                        PyExprStmt(
                            value = PyAwait(
                                value = PyCall(
                                    func = PyName(id = "other_func", ctx = PyExprContext.Load),
                                    args = mutableListOf(),
                                    keywords = mutableListOf()
                                )
                            )
                        )
                    ),
                    decoratorList = mutableListOf()
                )
            ),
            typeIgnores = mutableListOf()
        ).apply { assignParents() }
        assertEquals(
            """
            async def f():
                await other_func()
            """.trimIndent(),
            codeGenerator.generateToString(ast)
        )
    }
}
