package xyz.avarel.lobos.ast.expr

import xyz.avarel.lobos.lexer.Section

abstract class AbstractExpr(override val position: Section) : Expr