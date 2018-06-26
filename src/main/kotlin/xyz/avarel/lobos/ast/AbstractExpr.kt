package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type

abstract class AbstractExpr(
        override val type: Type,
        override val position: Section
): Expr