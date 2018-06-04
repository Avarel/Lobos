package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.filter
import xyz.avarel.lobos.parser.subtract
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.scope.ParserContext

object EqualsBinaryParser: BinaryOperatorParser(Precedence.EQUALITY, BinaryOperationType.EQUALS) {
    override fun parse(parser: Parser, scope: ParserContext, token: Token, left: Expr): Expr {
        val expr = super.parse(parser, scope, token, left) as BinaryOperation

        inferAssumptionExpr(scope, expr.right, expr.left, Type::filter to Type::subtract)?.let { (name, a, b) ->
            scope.assumptions[name] = a
            scope.inverseAssumptions[name] = b
        }
        inferAssumptionExpr(scope, expr.left, expr.right, Type::filter to Type::subtract)?.let { (name, a, b) ->
            scope.assumptions[name] = a
            scope.inverseAssumptions[name] = b
        }

        return expr
    }
}