package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.IndexAccessExpr
import xyz.avarel.lobos.ast.PropertyAccessExpr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.base.Namespace
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.complex.TupleType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object DotParser: InfixParser {
    override val precedence: Int get() = Precedence.DOT

    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        val ident = parser.eat()
        when (ident.type) {
            TokenType.INT -> {
                val leftType = if (left.type !is TupleType) {
                    throw SyntaxException("Expected tuple", left.position)
                } else left.type as TupleType

                val index = ident.string.toInt()

                val type = leftType.valueTypes.getOrNull(index) ?: throw SyntaxException("$leftType does not have index $index", ident.position)

                return IndexAccessExpr(type, left, index, token.position)
            }
            TokenType.IDENT -> {
                val name = ident.string

                val member = left.type.getMember(name)

                if (member == null) {
                    val implName = left.type.implNamespace!!

                    val implNamespace = scope.getAssumption(implName)?.type as? Namespace
                            ?: throw SyntaxException("There is no impl for ${left.type} in this scope", token.position)

                    val implMember = implNamespace.getMember(name) as? FunctionType
                            ?: throw SyntaxException("${left.type} does not have member/valid self function named $name", token.position)

                    val arguments = mutableListOf<Expr>().also { it += left }

                    parser.eat(TokenType.L_PAREN)
                    if (!parser.match(TokenType.R_PAREN)) {
                        do {
                            arguments += parser.parseExpr(scope, StmtContext())
                        } while (parser.match(TokenType.COMMA))
                        parser.eat(TokenType.R_PAREN)
                    }

                    val returnType = enhancedCheckInvocation(parser, true, implMember, arguments, stmt.expectedType, token.position)

                    if (returnType == NeverType) {
                        scope.terminates = true
                    }

                    return InvokeExpr(
                            returnType,
                            PropertyAccessExpr(
                                    implNamespace,
                                    IdentExpr(implName, implMember, token.position),
                                    name,
                                    token.position
                            ),
                            arguments,
                            token.position
                    )
                }

                return PropertyAccessExpr(member, left, name, token.position)
            }
            else -> throw SyntaxException("Invalid identifier", ident.position)
        }
    }
}