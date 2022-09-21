package com.strumenta.python.codegen

import com.strumenta.kolasu.model.Node

// This file contains the AST definition for the Python3 language
// It is partially based on the specifications https://docs.python.org/3/library/ast.html
// This could be potentially moved to a separate project in the future

// python AST node
sealed class PyNode : Node()

// modules
sealed class PyMod : PyNode()

data class PyModule(
    var body: List<PyStmt> = listOf(),
    var typeIgnores: List<PyTypeIgnore> = listOf()
) : PyMod()

data class PyInteractive(
    var body: List<PyStmt> = listOf()
) : PyMod()

data class PyExpression(
    var body: PyExpr
) : PyMod()

data class PyFunctionType(
    var argTypes: List<PyExpr> = listOf(),
    var returns: PyExpr
)

// statements
sealed class PyStmt : PyNode()

data class PyFunctionDef(
    var name: PyIdentifier,
    var args: PyArguments,
    var body: List<PyStmt> = listOf(),
    var decoratorList: List<PyExpr> = listOf(),
    var returns: PyExpr? = null,
    var typeComment: PyString? = null
) : PyStmt()

data class PyAsyncFunctionDef(
    var name: PyIdentifier,
    var args: PyArguments,
    var body: List<PyStmt> = listOf(),
    var decoratorList: List<PyExpr> = listOf(),
    var returns: PyExpr? = null,
    var typeComment: PyString? = null
) : PyStmt()

data class PyClassDef(
    var name: PyIdentifier? = null,
    var bases: List<PyExpr> = listOf(),
    var keywords: List<PyKeyword> = listOf(),
    var body: List<PyStmt> = listOf(),
    var decoratorList: List<PyExpr> = listOf()
) : PyStmt()

data class PyReturn(
    var value: PyExpr? = null
) : PyStmt()

data class PyDelete(
    var targets: List<PyExpr> = listOf()
) : PyStmt()

data class PyAssign(
    var targets: List<PyExpr> = listOf(),
    var value: PyExpr,
    var typeComment: PyString? = null
) : PyStmt()

data class PyAugAssign(
    var target: PyExpr,
    var op: PyOperator,
    var value: PyExpr
) : PyStmt()

data class PyAnnAssign(
    var target: PyExpr? = null,
    var annotation: PyExpr? = null,
    var value: PyExpr? = null,
    // 'simple' indicates that we annotate
    // simple name without parens
    var simple: PyInt = 0
) : PyStmt()

data class PyFor(
    var target: PyExpr,
    var iter: PyExpr,
    var body: List<PyStmt> = listOf(),
    // use 'orElse' because else is
    // a keyword in target languages
    var orElse: List<PyStmt> = listOf(),
    var typeComment: PyString? = null
) : PyStmt()

data class PyAsyncFor(
    var target: PyExpr,
    var iter: PyExpr,
    var body: List<PyStmt> = listOf(),
    var orElse: List<PyStmt> = listOf(),
    var typeComment: PyString? = null
) : PyStmt()

data class PyWhile(
    var test: PyExpr,
    var body: List<PyStmt> = listOf(),
    var orElse: List<PyStmt> = listOf()
) : PyStmt()

data class PyIf(
    var test: PyExpr,
    var body: List<PyStmt> = listOf(),
    var orElse: List<PyStmt> = listOf()
) : PyStmt()

data class PyWith(
    var items: List<PyWithItem> = listOf(),
    var body: List<PyStmt> = listOf(),
    var typeComment: PyString? = null
) : PyStmt()

data class PyAsyncWith(
    var items: List<PyWithItem> = listOf(),
    var body: List<PyStmt> = listOf(),
    var typeComment: PyString? = null
) : PyStmt()

data class PyMatch(
    var subject: PyExpr,
    var cases: List<PyMatchCase> = listOf()
) : PyStmt()

data class PyRaise(
    var exc: PyExpr? = null,
    var cause: PyExpr? = null
) : PyStmt()

data class PyTry(
    var body: List<PyStmt> = listOf(),
    var handlers: List<PyExceptHandler> = listOf(),
    var orElse: List<PyStmt> = listOf(),
    var finalBody: List<PyStmt> = listOf()
) : PyStmt()

data class PyTryStar(
    var body: List<PyStmt> = listOf(),
    var handlers: List<PyExceptHandler> = listOf(),
    var orElse: List<PyStmt> = listOf(),
    var finalBody: List<PyStmt> = listOf()
) : PyStmt()

data class PyAssert(
    var test: PyExpr,
    var msg: PyExpr? = null
) : PyStmt()

data class PyImport(
    var names: List<PyAlias> = listOf()
) : PyStmt()

data class PyImportFrom(
    var module: PyIdentifier? = null,
    var names: List<PyAlias> = listOf(),
    var level: PyInt? = null
) : PyStmt()

data class PyGlobal(
    var names: List<PyIdentifier> = listOf()
) : PyStmt()

data class PyNonLocal(
    var names: List<PyIdentifier> = listOf()
) : PyStmt()

data class PyExprStmt(
    var value: PyExpr
) : PyStmt()

class PyPass : PyStmt()

class PyBreak : PyStmt()

class PyContinue : PyStmt()

// expressions
sealed class PyExpr : PyNode()

data class PyBoolOpExpr(
    var op: PyBoolOp,
    var values: List<PyExpr>
) : PyExpr()

data class PyNamedExpr(
    var target: PyExpr,
    var value: PyExpr
) : PyExpr()

data class PyBinOp(
    var left: PyExpr,
    var op: PyOperator,
    var right: PyExpr
) : PyExpr()

data class PyUnaryOpExpr(
    var op: PyUnaryOp,
    var operand: PyExpr
) : PyExpr()

data class PyLambda(
    var args: PyArguments,
    var body: PyExpr
) : PyExpr()

data class PyIfExp(
    var test: PyExpr,
    var body: PyExpr,
    var orElse: PyExpr
) : PyExpr()

data class PyDict(
    var keys: List<PyExpr> = listOf(),
    var values: List<PyExpr> = listOf()
) : PyExpr()

data class PySet(
    var elts: List<PyExpr> = listOf()
) : PyExpr()

data class PyListComp(
    var elt: PyExpr,
    var generators: List<PyComprehension> = listOf()
) : PyExpr()

data class PySetComp(
    var elt: PyExpr,
    var generators: List<PyComprehension> = listOf()
) : PyExpr()

data class PyDictComp(
    var key: PyExpr,
    var value: PyExpr,
    var generators: List<PyComprehension> = listOf()
) : PyExpr()

data class PyGeneratorExp(
    var elt: PyExpr,
    var generators: List<PyComprehension> = listOf()
) : PyExpr()

data class PyAwait(
    var value: PyExpr
) : PyExpr()

data class PyYield(
    var value: PyExpr? = null
) : PyExpr()

data class PyYieldFrom(
    var value: PyExpr
) : PyExpr()

// need sequences for compare to distinguish
// between x < 4 < 3 and (x < 4) < 3
data class PyCompare(
    var left: PyExpr,
    var ops: List<PyCmpOp> = listOf(),
    var comparators: List<PyExpr> = listOf()
) : PyExpr()

data class PyCall(
    var func: PyExpr,
    var args: List<PyExpr> = listOf(),
    var keywords: List<PyKeyword> = listOf()
) : PyExpr()

data class PyFormattedValue(
    var value: PyExpr,
    var conversion: PyInt,
    var formatSpec: PyExpr? = null
) : PyExpr()

data class PyJoinedStr(
    var values: List<PyExpr> = listOf()
) : PyExpr()

data class PyConstantExpr(
    var value: PyConstant,
    var kind: PyString? = null
) : PyExpr()

// the following expression can appear
// in assignment context

data class PyAttribute(
    var value: PyExpr,
    var attr: PyIdentifier,
    var ctx: PyExprContext
) : PyExpr()

data class PySubscript(
    var value: PyExpr,
    var slice: PyExpr,
    var ctx: PyExprContext
) : PyExpr()

data class PyStarred(
    var value: PyExpr,
    var ctx: PyExprContext
) : PyExpr()

data class PyName(
    var id: PyIdentifier,
    var ctx: PyExprContext
) : PyExpr()

data class PyList(
    var elts: List<PyExpr> = listOf(),
    var ctx: PyExprContext
) : PyExpr()

data class PyTuple(
    var elts: List<PyExpr> = listOf(),
    var ctx: PyExprContext
) : PyExpr()

// can appear only in Subscript
data class PySlice(
    var lower: PyExpr? = null,
    var upper: PyExpr? = null,
    var step: PyExpr? = null
) : PyExpr()

enum class PyExprContext { Load, Store, Del }

enum class PyBoolOp { And, Or }

enum class PyOperator {
    Add, Sub, Mult, MatMult, Div, Mod, Pow, LShift,
    RShift, BitOr, BitXor, BitAnd, FloorDiv
}

enum class PyUnaryOp {
    Invert, Not, UAdd, USub
}

enum class PyCmpOp {
    Eq, NotEq, Lt, LtE, Gt, GtE, Is, IsNot, In, NotIn
}

data class PyComprehension(
    var target: PyExpr,
    var iter: PyExpr,
    var ifs: List<PyExpr> = listOf(),
    var isAsync: PyInt
) : PyNode()

data class PyExceptHandler(
    var type: PyExpr? = null,
    var name: PyIdentifier? = null,
    var body: List<PyStmt> = listOf()
) : PyNode()

data class PyArguments(
    var posOnlyArgs: List<PyArg> = listOf(),
    var args: List<PyArg> = listOf(),
    var varArg: PyArg? = null,
    var kwOnlyArgs: List<PyArg> = listOf(),
    var kwArg: PyArg? = null
) : PyNode()

data class PyArg(
    var arg: PyIdentifier,
    var annotation: PyExpr? = null,
    var default: PyExpr? = null,
    var typeComment: PyString? = null,
) : PyNode()

// keyword arguments supplied
// to call (NULL identifier for **kwargs)
data class PyKeyword(
    var arg: PyIdentifier? = null,
    var value: PyExpr
) : PyNode()

// import name with optional 'as' alias
data class PyAlias(
    var name: PyIdentifier,
    var asName: PyIdentifier? = null
) : PyNode()

data class PyWithItem(
    var contextExpr: PyExpr,
    var optionalVars: PyExpr? = null
) : PyNode()

data class PyMatchCase(
    var pattern: PyPattern,
    var guard: PyExpr? = null,
    var body: List<PyStmt> = listOf()
) : PyNode()

// patterns
sealed class PyPattern : PyNode()

data class PyMatchValue(
    var value: PyExpr
) : PyPattern()

data class PyMatchSingleton(
    var value: PyConstant
) : PyPattern()

data class PyMatchSequence(
    var patterns: List<PyPattern> = listOf()
) : PyPattern()

data class PyMatchMapping(
    var keys: List<PyExpr> = listOf(),
    var patterns: List<PyPattern> = listOf(),
    // The optional 'rest' MatchMapping parameter
    // handles capturing extra mapping keys
    var rest: PyIdentifier? = null
) : PyPattern()

data class PyMatchClass(
    var cls: PyExpr,
    var patterns: List<PyPattern> = listOf(),
    var kwdAttrs: List<PyIdentifier> = listOf(),
    var kwdPatterns: List<PyPattern> = listOf()
) : PyPattern()

data class PyMatchStar(
    var name: PyIdentifier? = null
) : PyPattern()

data class PyMatchAs(
    var pattern: PyPattern? = null,
    var name: PyIdentifier? = null
) : PyPattern()

data class PyMatchOr(
    var patterns: List<PyPattern> = listOf()
) : PyPattern()

data class PyTypeIgnore(
    // val lineNo: PyInt,
    var tag: PyString
) : PyNode()

typealias PyInt = Int
typealias PyString = String
typealias PyConstant = String
typealias PyIdentifier = String
