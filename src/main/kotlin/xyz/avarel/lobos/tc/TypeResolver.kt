package xyz.avarel.lobos.tc

import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.ast.types.basic.IdentTypeAST
import xyz.avarel.lobos.ast.types.basic.NeverTypeAST
import xyz.avarel.lobos.ast.types.basic.NullTypeAST
import xyz.avarel.lobos.ast.types.complex.FunctionTypeAST
import xyz.avarel.lobos.ast.types.complex.TemplatingTypeAST
import xyz.avarel.lobos.ast.types.complex.TupleTypeAST
import xyz.avarel.lobos.ast.types.complex.UnionTypeAST
import xyz.avarel.lobos.parser.TypeException
import xyz.avarel.lobos.tc.base.InvalidType
import xyz.avarel.lobos.tc.base.NeverType
import xyz.avarel.lobos.tc.base.NullType
import xyz.avarel.lobos.tc.base.UnitType
import xyz.avarel.lobos.tc.complex.FunctionType
import xyz.avarel.lobos.tc.complex.TupleType
import xyz.avarel.lobos.tc.scope.ScopeContext

class TypeResolver(
        val scope: ScopeContext,
        val errorHandler: (TypeException) -> Unit
) : TypeASTVisitor<Type> {
    override fun visit(typeAst: IdentTypeAST): Type {
        val type = scope.getType(typeAst.name)

        if (type == null) {
            errorHandler(TypeException("Unresolved type ${typeAst.name}", typeAst.position))
        }

        return type ?: InvalidType
    }

    override fun visit(typeAst: NeverTypeAST) = NeverType

    override fun visit(typeAst: NullTypeAST) = NullType

    override fun visit(typeAst: FunctionTypeAST): Type {
        return FunctionType(
                typeAst.arguments.map { it.accept(this) },
                typeAst.returnType.accept(this)
        )
    }

    override fun visit(typeAst: TupleTypeAST): Type {
        if (typeAst.types.isEmpty()) return UnitType

        return TupleType(typeAst.types.map { it.accept(this) })
    }

    override fun visit(typeAst: UnionTypeAST): Type {
        val left = typeAst.left.accept(this)
        val right = typeAst.right.accept(this)
        return (left.toList() + right.toList()).toType()
    }

    override fun visit(typeAst: TemplatingTypeAST): Type {
        val target = typeAst.target.accept(this)

        if (target !is TypeTemplate) {
            errorHandler(TypeException("$target is not a generic type", typeAst.target.position))
            return InvalidType
        }

        val arguments = typeAst.arguments.map { it.accept(this) }

        if (arguments.size != target.genericParameters.size) {
            errorHandler(TypeException("Expected ${target.genericParameters.size} type arguments, found ${arguments.size} arguments", typeAst.position))
            return InvalidType
        }

        return target.template(arguments)
    }
}