package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.generics.UnionType

interface ExistentialType: Type {
    override val universalType: Type
}

class LiteralIntRangeInclusiveType(val start: Int, val end: Int): ExistentialType {
    override val universalType: Type get() = I32Type
    override val parentType get() = I32Type

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other is UnionType -> other.valueTypes.all(this::isAssignableFrom)
            other is LiteralIntType -> other.value in start..end
            other is LiteralIntRangeInclusiveType -> other.start in start..end && other.end in start..end
            other is LiteralIntRangeExclusiveType -> other.start in start..end && other.end in start..end + 1
            else -> false
        }
    }

    override fun toString() = "$start..=$end"

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralIntRangeInclusiveType -> false
            else -> start == other.start && end == other.end
        }
    }
}

class LiteralIntRangeExclusiveType(val start: Int, val end: Int): ExistentialType {
    override val universalType: Type get() = I32Type
    override val parentType by lazy { LiteralIntRangeInclusiveType(start, end - 1) }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other is UnionType -> other.valueTypes.all(this::isAssignableFrom)
            other is LiteralIntType -> other.value in start until end
            other is LiteralIntRangeInclusiveType -> other.start in start until end && other.end in start until end
            other is LiteralIntRangeExclusiveType -> other.start in start..end && other.end in start..end
            else -> false
        }
    }

    override fun toString() = "$start..<$end"

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralIntRangeExclusiveType -> false
            else -> start == other.start && end == other.end
        }
    }
}

class LiteralIntType(val value: Int): ExistentialType {
    override val universalType: Type get() = I32Type
    override val parentType by lazy { LiteralIntRangeInclusiveType(value, value) }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other !is LiteralIntType -> false
            else -> other.value == value
        }
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralIntType -> false
            else -> value == other.value
        }
    }
}

class LiteralStringType(val value: String): ExistentialType {
    override val universalType: Type get() = StrType
    override val parentType get() = StrType

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other !is LiteralStringType -> false
            else -> other.value == value
        }
    }

    override fun toString() = "\"$value\""

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralStringType -> false
            else -> value == other.value
        }
    }
}

object LiteralTrueType: ExistentialType {
    override val universalType: Type get() = BoolType
    override val parentType: Type get() = BoolType

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other
    }

    override fun toString() = "true"
}

object LiteralFalseType: ExistentialType {
    override val universalType: Type get() = BoolType
    override val parentType: Type get() = BoolType

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other
    }

    override fun toString() = "false"
}