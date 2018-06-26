package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object IfParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val conditionCtx = StmtContext(BoolType)

        val condition = parser.parseExpr(scope, conditionCtx)

        typeCheck(BoolType, condition.type, condition.position)

        val thenScope = scope.subContext()

        thenScope.assumptions += conditionCtx.assumptions

        val thenBranch = parser.parseBlock(thenScope)

        val elseBranch: Expr?
        val elseScope: ScopeContext?

        if (parser.match(TokenType.ELSE)) {
            elseScope = scope.subContext()
            elseScope.assumptions += conditionCtx.inverseAssumptions
            elseBranch = if (parser.nextIs(TokenType.IF)) parser.parseExpr(elseScope, StmtContext())
            else parser.parseBlock(elseScope)
        } else {
            if (stmt.expectedType != null) {
                parser.errors += SyntaxException("if expression must have else branch", token.position)
            }
            elseBranch = null
            elseScope = null
        }

        if (thenScope.terminates) {
            scope.assumptions += conditionCtx.inverseAssumptions
        }

        val type = when {
            elseBranch != null -> when {
                elseScope!!.terminates -> if (thenScope.terminates) NeverType else thenBranch.type
                else -> elseBranch.type.commonAssignableToType(thenBranch.type)
            }
            else -> InvalidType
        }

        return IfExpr(type, condition, thenBranch, elseBranch, token.position)
    }
}