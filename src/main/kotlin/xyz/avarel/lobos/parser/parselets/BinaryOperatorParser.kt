package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.enhancedCheckInvocation
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.base.Namespace
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

open class BinaryOperatorParser(precedence: Int, val operator: BinaryOperationType, leftAssoc: Boolean = true): BinaryParser(precedence, leftAssoc) {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, StmtContext(AnyType), precedence - if (leftAssoc) 0 else 1)

        val implName = left.type.implNamespace!!

        val implNamespace = scope.getAssumption(implName)?.type as? Namespace
                ?: throw SyntaxException("There is no impl for ${left.type} in this scope", token.position)

        val member = implNamespace.getMember(operator.functionName)
                ?: throw SyntaxException("${left.type} does not have a ${operator.functionName} operation", token.position)

        val returnType = enhancedCheckInvocation(parser, true, member, listOf(left, right), stmt.expectedType, token.position)

        return BinaryOperation(returnType, left, right, operator, token.position)
    }
}