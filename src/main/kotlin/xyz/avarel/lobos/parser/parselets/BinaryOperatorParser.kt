package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ops.BinaryOperation
import xyz.avarel.lobos.ast.expr.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.span
import xyz.avarel.lobos.parser.Parser

class BinaryOperatorParser(precedence: Int, val operator: BinaryOperationType, leftAssoc: Boolean = true) : BinaryParser(precedence, leftAssoc) {
    override fun parse(parser: Parser, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(precedence - if (leftAssoc) 0 else 1)
        return BinaryOperation(left, right, operator, left.span(right))
    }
//
//    private fun resolveBinaryOpType(operationType: BinaryOperationType, left: Type, right: Type, position: Section): Type {
//        when (left) {
//            StrType,
//            is LiteralStrType -> if (operationType == BinaryOperationType.ADD) return StrType
//            I32Type,
//            is LiteralIntType -> when (right) {
//                I32Type,
//                is LiteralIntType -> return I32Type
//                I64Type,
//                F64Type -> return right
//            }
//            I64Type -> when (right) {
//                I32Type,
//                is LiteralIntType,
//                I64Type -> return I64Type
//                F64Type -> F64Type
//            }
//            F64Type -> when (right) {
//                I32Type,
//                is LiteralIntType,
//                I64Type,
//                F64Type -> return F64Type
//            }
//        }
//        throw SyntaxException("$left is incompatible with $right", position)
//    }
}