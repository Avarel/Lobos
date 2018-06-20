package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.enhancedCheckInvocation
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

open class BinaryOperatorParser(precedence: Int, val operator: BinaryOperationType, leftAssoc: Boolean = true): BinaryParser(precedence, leftAssoc) {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, StmtContext(AnyType), precedence - if (leftAssoc) 0 else 1)

        val member = left.type.getMember(operator.functionName)
                ?: throw SyntaxException("${left.type} does not have a ${operator.functionName} operation", token.position)

        val returnType = enhancedCheckInvocation(parser, member, listOf(left, right), ctx.expectedType, token.position)

        return BinaryOperation(returnType, left, right, operator, token.position)
    }
}