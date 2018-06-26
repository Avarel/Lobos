package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.*
import xyz.avarel.lobos.typesystem.literals.LiteralIntType
import xyz.avarel.lobos.typesystem.literals.LiteralStrType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

class BinaryOperatorParser(precedence: Int, val operator: BinaryOperationType, leftAssoc: Boolean = true) : BinaryParser(precedence, leftAssoc) {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, StmtContext(AnyType), precedence - if (leftAssoc) 0 else 1)
        val position = left.position.span(right.position)

        val returnType = resolveBinaryOpType(operator, left.type, right.type, position)

        return BinaryOperation(returnType, left, right, operator, position)
    }

    private fun resolveBinaryOpType(operationType: BinaryOperationType, left: Type, right: Type, position: Section): Type {
        when (left) {
            StrType,
            is LiteralStrType -> if (operationType == BinaryOperationType.ADD) return StrType
            I32Type,
            is LiteralIntType -> when (right) {
                I32Type,
                is LiteralIntType -> return I32Type
                I64Type,
                F64Type -> return right
            }
            I64Type -> when (right) {
                I32Type,
                is LiteralIntType,
                I64Type -> return I64Type
                F64Type -> F64Type
            }
            F64Type -> when (right) {
                I32Type,
                is LiteralIntType,
                I64Type,
                F64Type -> return F64Type
            }
        }
        throw SyntaxException("$left is incompatible with $right", position)
    }
}