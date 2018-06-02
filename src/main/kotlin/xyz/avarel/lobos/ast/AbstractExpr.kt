package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.Type

abstract class AbstractExpr(
        override val type: Type,
        override val position: Position
): Expr