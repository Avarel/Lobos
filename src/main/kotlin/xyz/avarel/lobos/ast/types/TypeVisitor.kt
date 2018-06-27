package xyz.avarel.lobos.ast.types

import xyz.avarel.lobos.ast.types.basic.IdentTypeAST
import xyz.avarel.lobos.ast.types.basic.NeverTypeAST
import xyz.avarel.lobos.ast.types.basic.NullTypeAST
import xyz.avarel.lobos.ast.types.complex.FunctionTypeAST
import xyz.avarel.lobos.ast.types.complex.TemplatingTypeAST
import xyz.avarel.lobos.ast.types.complex.TupleTypeAST
import xyz.avarel.lobos.ast.types.complex.UnionTypeAST

interface TypeVisitor<R> {
    fun visit(type: IdentTypeAST): R
    fun visit(type: NeverTypeAST): R
    fun visit(type: NullTypeAST): R
    fun visit(type: FunctionTypeAST): R
    fun visit(type: TupleTypeAST): R
    fun visit(type: UnionTypeAST): R
    fun visit(type: TemplatingTypeAST): R
}