package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.DummyExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.LetExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object LetParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        if (stmt.expectedType != null) {
            throw SyntaxException("Not an expression", token.position)
        }

        val mutable = parser.match(TokenType.MUT)

        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val type: Type? = if (parser.match(TokenType.COLON)) parser.parseType(scope) else null

        if (Modifier.EXTERNAL in stmt.modifiers) {
            if (type == null) {
                throw SyntaxException("Type annotation required for extern definitions", token.position)
            }
            scope.variables[name] = VariableInfo(mutable, type)
            return DummyExpr
        }

        val assign = parser.eat(TokenType.ASSIGN)

        val expr = parser.parseExpr(scope, StmtContext(type ?: AnyType))

        if (name in scope.variables) {
            throw SyntaxException("Variable $name has already been declared", ident.position)
        } else {
            if (type != null) {
                val exprType = enhancedInfer(parser, type, expr.type, assign.position)

                if (type.isAssignableFrom(exprType)) {
                    scope.variables[name] = VariableInfo(mutable, type)
                    if (expr.type != type) { // If assumption is necessary
                        scope.assumptions[name] = VariableInfo(mutable, exprType)
                    }
                } else {
                    scope.variables[name] = VariableInfo(mutable, type)
                    throw SyntaxException("Expected $type but found $exprType", assign.position)
                }
            } else {
                val exprType = expr.type
                if (exprType is TypeTemplate && exprType.genericParameters.isNotEmpty()) {
                    throw SyntaxException("Not enough information to infer type parameters of expression", token.position)
                }

                scope.variables[name] = VariableInfo(mutable, exprType.universalType)
                scope.assumptions[name] = VariableInfo(mutable, exprType)
            }
            return LetExpr(name, expr, token.position)
        }
    }
}