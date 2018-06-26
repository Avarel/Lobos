package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.EqualsOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object EqualsBinaryParser: InfixParser {
    override val precedence: Int get() = Precedence.EQUALITY

    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, StmtContext(AnyType), precedence)

        if (!left.type.isAssignableFrom(right.type) && !right.type.isAssignableFrom(left.type)) {
            parser.errors += SyntaxException("${left.type} and ${right.type} are incompatible", token.position)
        }

        inferAssumptionExpr(
                true,
                scope,
                stmt,
                left,
                right,
                Type::filter to Type::exclude
        )?.let { (name, a, b) ->
            stmt.assumptions[name] = a
            stmt.inverseAssumptions[name] = b
        }

        inferAssumptionExpr(
                true,
                scope,
                stmt,
                right,
                left,
                Type::filter to Type::exclude
        )?.let { (name, a, b) ->
            stmt.assumptions[name] = a
            stmt.inverseAssumptions[name] = b
        }

        return EqualsOperation(left, right, left.position.span(right.position))
    }
}