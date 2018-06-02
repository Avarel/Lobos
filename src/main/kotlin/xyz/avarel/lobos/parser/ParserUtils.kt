package xyz.avarel.lobos.parser

import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.*
import xyz.avarel.lobos.typesystem.generics.TupleType
import xyz.avarel.lobos.typesystem.generics.UnionType
import xyz.avarel.lobos.typesystem.scope.ParserContext

fun Parser.parseType(scope: ParserContext): Type {
    val type = parseSingleType(scope)

    if (match(TokenType.PIPE)) {
        val list = mutableListOf<Type>()
        list.add(type)
        do {
            list.add(try { parseSingleType(scope) } catch (e: SyntaxException) { InvalidType })
        } while (match(TokenType.PIPE))

        val optimizedList = list.distinct()

        return when {
            optimizedList.size == 2 && LiteralTrueType in optimizedList && LiteralFalseType in optimizedList -> BoolType
            AnyType in optimizedList -> AnyType
            else -> UnionType(optimizedList)
        }
    }

    return type
}

fun Parser.parseSingleType(scope: ParserContext): Type {
    return when {
        match(TokenType.TRUE) -> LiteralTrueType
        match(TokenType.FALSE) -> LiteralFalseType
        match(TokenType.IDENT) -> {
            val ident = last
            val name = ident.string!!
            scope.getType(name) ?: throw SyntaxException("Unresolved type $name", ident.position)
        }
        match(TokenType.INT) -> {
            val start = last.string!!.toInt()
            when {
                match(TokenType.RANGE_IN) -> {
                    val endExpr = eat(TokenType.INT)
                    val end = endExpr.string!!.toInt()

                    when {
                        end < start -> throw SyntaxException("start[$start] must be <= end[$end]", endExpr.position)
                        end == start -> LiteralIntType(start)
                        else -> LiteralIntRangeInclusiveType(start, end)
                    }
                }
                match(TokenType.RANGE_EX) -> {
                    val endExpr = eat(TokenType.INT)
                    val end = endExpr.string!!.toInt()

                    when {
                        end <= start -> throw SyntaxException("start[$start] must be < end[$end]", endExpr.position)
                        else -> LiteralIntRangeExclusiveType(start, end)
                    }
                }
                else -> LiteralIntType(start)
            }
        }
        match(TokenType.STRING) -> LiteralStringType(last.string!!)
        match(TokenType.L_PAREN) -> {
            when {
                match(TokenType.R_PAREN) -> TupleType.Unit
                else -> {
                    val valueTypes = mutableListOf<Type>()
                    do {
                        valueTypes.add(try { parseType(scope) } catch (e: SyntaxException) { InvalidType })
                    } while (match(TokenType.COMMA))
                    eat(TokenType.R_PAREN)
                    TupleType(valueTypes)
                }
            }
        }
        else -> throw SyntaxException("Expected type", peek().position)
    }
}