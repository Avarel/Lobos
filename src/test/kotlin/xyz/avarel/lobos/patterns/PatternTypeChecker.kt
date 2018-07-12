package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.tc.Type
import xyz.avarel.lobos.tc.TypeChecker
import xyz.avarel.lobos.tc.base.I32Type
import xyz.avarel.lobos.tc.base.StrType
import xyz.avarel.lobos.tc.complex.TupleType
import xyz.avarel.lobos.tc.scope.ScopeContext

class PatternTypeChecker(
        val tc: TypeChecker,
        val targetType: Type,
        val scope: ScopeContext
) : PatternVisitor<Boolean> {
    override fun visit(pattern: WildcardPattern): Boolean {
        return true
    }

    override fun visit(pattern: I32Pattern): Boolean {
        tc.checkType(targetType, I32Type, pattern.section)
        return false
    }

    override fun visit(pattern: StrPattern): Boolean {
        tc.checkType(targetType, StrType, pattern.section)
        return false
    }

    override fun visit(pattern: TuplePattern): Boolean {
        if (targetType !is TupleType) {
            tc.errorHandler("$targetType can not be matched to a tuple pattern", pattern.section)
            return false
        }

        if (targetType.valueTypes.size != pattern.list.size) {
            tc.errorHandler("Tuple pattern size mismatch, expected ${pattern.list.size}, found ${targetType.valueTypes.size}", pattern.section)
            return false
        }

        return pattern.list.zip(targetType.valueTypes)
                .map { (pattern, type) -> pattern.accept(PatternTypeChecker(tc, type, scope)) }
                .all { it }
    }

    override fun visit(pattern: VariablePattern): Boolean {
        if (pattern.type == null) {
            scope.declare(pattern.name, targetType.universalType, pattern.mutable)
        } else {
            val type = tc.run { pattern.type.resolve(scope) }

            scope.declare(pattern.name, type, pattern.mutable)

            if (type.isAssignableFrom(targetType)) {
                scope.assume(pattern.name, targetType)
            } else {
                tc.errorHandler("Expected $type but found $targetType", pattern.section)
            }
        }

        return false
    }
}