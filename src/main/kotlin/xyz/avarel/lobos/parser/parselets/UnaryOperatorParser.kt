package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.UnaryOperation
import xyz.avarel.lobos.ast.ops.UnaryOperationType
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.base.F64Type
import xyz.avarel.lobos.typesystem.base.I32Type
import xyz.avarel.lobos.typesystem.base.I64Type
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

class UnaryOperatorParser(val operator: UnaryOperationType) : PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val target = parser.parseExpr(scope, StmtContext(AnyType))

        val returnType = resolveUnaryOpType(operator, target.type, token.position.span(target.position))

        return UnaryOperation(returnType, target, operator, token.position.span(target.position))
    }

    private fun resolveUnaryOpType(operationType: UnaryOperationType, target: Type, position: Section): Type {
        when (target) {
            I32Type,
            I64Type,
            F64Type -> return target
        }
        throw SyntaxException("$target is incompatible", position)
    }
}