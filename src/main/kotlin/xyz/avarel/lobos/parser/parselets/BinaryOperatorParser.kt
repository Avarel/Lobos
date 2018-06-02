package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.typesystem.base.StrType
import xyz.avarel.lobos.typesystem.scope.ParserContext

class BinaryOperatorParser(precedence: Int, val operator: BinaryOperationType, leftAssoc: Boolean = true): BinaryParser(precedence, leftAssoc) {
    override fun parse(parser: Parser, scope: ParserContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, precedence - if (leftAssoc) 0 else 1)

        // TODO more exhaustive type checks

        // This assumption is safe since literal string types do not exist at runtime
        if (left.type.universalType == StrType && operator == BinaryOperationType.ADD) {
            return BinaryOperation(StrType, left, right, operator, token.position)
        }

        return BinaryOperation(left, right, operator, token.position)
    }
}