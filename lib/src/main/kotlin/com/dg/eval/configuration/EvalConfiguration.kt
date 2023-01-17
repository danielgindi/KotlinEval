package com.dg.eval.configuration

import com.dg.eval.StringConversion
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.math.pow

open class EvalConfiguration :
        MathOperations,
        LogicalOperations,
        BitwiseOperations,
        ComparisonOperations,
        ConversionOperations
{
    constructor(populateDefaults: Boolean = true,
                autoParseNumericStrings: Boolean = true,
                autoParseNumericStringsLocale: Locale? = null,
                operatorOrder: List<List<String>>? = null,
                prefixOperators: Set<String>? = null,
                suffixOperators: Set<String>? = null,
                rightAssociativeOps: Set<String>? = null,
                varNameChars: Set<Char>? = null,
                genericConstants: MutableMap<String, Any?>? = null,
                genericFunctions: MutableMap<String, EvalFunctionBlock>? = null,
                constants: MutableMap<String, Any?>? = null,
                functions: MutableMap<String, EvalFunctionBlock>? = null,
                constProvider: ConstProvider? = null)
    {
        this.autoParseNumericStrings = autoParseNumericStrings
        this.autoParseNumericStringsLocale = autoParseNumericStringsLocale
        this.operatorOrder = operatorOrder ?:
                if (populateDefaults) Defaults.defaultOperatorOrder
                else listOf()
        this.prefixOperators = prefixOperators ?:
                if (populateDefaults) Defaults.defaultPrefixOperators
                else setOf()
        this.suffixOperators = suffixOperators ?:
                if (populateDefaults) Defaults.defaultSuffixOperators
                else setOf()
        this.rightAssociativeOps = rightAssociativeOps ?:
                if (populateDefaults) Defaults.defaultRightAssociativeOps
                else setOf()
        this.varNameChars = varNameChars ?:
                if (populateDefaults) Defaults.defaultVarNameChars
                else setOf()
        this.genericConstants = genericConstants ?:
                if (populateDefaults) Defaults.defaultGenericConstants.toMutableMap()
                else mutableMapOf()
        this.genericFunctions = genericFunctions ?:
                if (populateDefaults)
                    Defaults.getDefaultGenericFunctions(
                            autoParseNumericStrings = autoParseNumericStrings,
                            autoParseNumericStringsLocale = autoParseNumericStringsLocale
                    ).toMutableMap()
                else mutableMapOf()
        this.constants = constants
        this.functions = functions
        this.constProvider = constProvider
    }

    var autoParseNumericStrings: Boolean
    var autoParseNumericStringsLocale: Locale?

    internal var allOperators = listOf<String>()
    private var _operatorOrder = listOf<List<String>>()
    var operatorOrder: List<List<String>>
        get()
        {
            return _operatorOrder
        }
        set(newValue)
        {
            val ops = mutableListOf<String>()
            for (ops2 in newValue)
                ops.addAll(ops2)

            _operatorOrder = newValue
            allOperators = ops
        }

    var prefixOperators: Set<String> = setOf()
    var suffixOperators: Set<String> = setOf()

    // https://en.wikipedia.org/wiki/Operator_associativity
    var rightAssociativeOps = setOf<String>()

    var varNameChars = setOf<Char>()

    var genericConstants = mutableMapOf<String, Any?>()
    var genericFunctions = mutableMapOf<String, EvalFunctionBlock>()

    var constants: MutableMap<String, Any?>? = null
    var functions: MutableMap<String, EvalFunctionBlock>? = null

    /**
     * A provider for constants that are not defined in the configuration.
     * This is useful for providing constants that are not known at compile time.
     * Return <code>Evaluator.ConstProviderDefault</code> to fall back to the default behavior.
     */
    var constProvider: ConstProvider? = null

    @Suppress("unused")
    open fun setConstant(name: String, value: Any?)
    {
        if (constants == null)
        {
            constants = mutableMapOf()
        }
        constants!![name] = value
    }

    @Suppress("unused")
    open fun removeConstant(name: String)
    {
        if (constants == null)
        {
            return
        }
        constants?.remove(name)
    }

    @Suppress("unused")
    open fun setFunction(name: String, func: EvalFunctionBlock)
    {
        if (functions == null)
        {
            functions = mutableMapOf()
        }
        functions!![name] = func
    }

    @Suppress("unused")
    open fun removeFunction(name: String)
    {
        if (functions == null)
        {
            return
        }
        functions?.remove(name)
    }

    @Suppress("unused")
    open fun clearConstants()
    {
        constants?.clear()
    }

    @Suppress("unused")
    open fun clearFunctions()
    {
        functions?.clear()
    }

    open fun clone(deep: Boolean = false): EvalConfiguration
    {
        return EvalConfiguration(
                populateDefaults = false,
                autoParseNumericStrings = autoParseNumericStrings,
                autoParseNumericStringsLocale = autoParseNumericStringsLocale,
                operatorOrder = operatorOrder,
                prefixOperators = prefixOperators,
                suffixOperators = suffixOperators,
                rightAssociativeOps = rightAssociativeOps,
                varNameChars = varNameChars,
                genericConstants =
                if (deep) genericConstants.toMutableMap()
                else genericConstants,
                genericFunctions =
                if (deep) genericFunctions.toMutableMap()
                else genericFunctions,
                constants =
                if (deep) constants?.toMutableMap()
                else constants,
                functions =
                if (deep) functions?.toMutableMap()
                else functions,
                constProvider = constProvider,
        )
    }

    //region LogicalProtocol

    override fun isTruthy(a: Any?): Boolean
    {
        if (a == null)
        {
            return false
        }

        if (a is String)
            return a.length > 0

        if (a is Boolean)
            return a

        if (a is Collection<*>)
            return !a.isEmpty()

        if (a is Double)
            return a != 0.0

        if (a is Float)
            return a != 0.0f

        if (a is Number)
            return a.toDouble() != 0.0

        return true
    }

    override fun logicalNot(a: Any?): Boolean =
            !isTruthy(a)

    //endregion

    //region ComparisonProtocol

    override fun compare(a: Any?, b: Any?): Int?
    {
        if (a == null && b == null)
            return 0

        if (a == null)
            return -1

        if (b == null)
            return 1

        if (a is Double && b is Double)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is Float && b is Float)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is Int && b is Int)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is Long && b is Long)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is BigDecimal && b is BigDecimal)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is BigInteger && b is BigInteger)
            return if (a < b) -1 else if (a > b) 1 else 0

        if (a is Boolean && b is Boolean)
            return if (!a && b) -1 else if (a && !b) 1 else 0

        if (a is String && b is String)
        {
            return a.compareTo(b)
        }

        if (a is Double || b is Double)
        {
            val na = (StringConversion.optionallyConvertStringToNumber(a,
                    locale = autoParseNumericStringsLocale) as? Number)?.toDouble()
            val nb = (StringConversion.optionallyConvertStringToNumber(b,
                    locale = autoParseNumericStringsLocale) as? Number)?.toDouble()
            if (na == null || nb == null)
                return null

            return if (na < nb) -1 else if (na > nb) 1 else 0
        }

        return null
    }

    override fun lessThan(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res == -1
    }

    override fun lessThanOrEqualsTo(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res == -1 || res == 0
    }

    override fun greaterThan(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res == 1
    }

    override fun greaterThanOrEqualsTo(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res == 1 || res == 0
    }

    override fun equalsTo(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res == 0
    }

    override fun notEqualsTo(a: Any?, b: Any?): Boolean
    {
        val res = compare(a = a, b = b) ?: return false
        return res != 0
    }

    //endregion

    //region MathProtocol

    override fun add(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a + b

        if (a is Float && b is Float)
            return a + b

        if (a is Int && b is Int)
            return a + b

        if (a is Long && b is Long)
            return a + b

        if (a is BigDecimal && b is BigDecimal)
            return a + b

        if (a is BigInteger && b is BigInteger)
            return a + b

        throw IllegalArgumentException("add(a,b) is not implemented for this kind of value")
    }

    override fun subtract(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a - b

        if (a is Float && b is Float)
            return a - b

        if (a is Int && b is Int)
            return a - b

        if (a is Long && b is Long)
            return a - b

        if (a is BigDecimal && b is BigDecimal)
            return a - b

        if (a is BigInteger && b is BigInteger)
            return a - b

        throw IllegalArgumentException("subtract(a,b) is not implemented for this kind of value")
    }

    override fun multiply(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a * b

        if (a is Float && b is Float)
            return a * b

        if (a is Int && b is Int)
            return a * b

        if (a is Long && b is Long)
            return a * b

        if (a is BigDecimal && b is BigDecimal)
            return a * b

        if (a is BigInteger && b is BigInteger)
            return a * b

        throw IllegalArgumentException("multiply(a,b) is not implemented for this kind of value")
    }

    override fun divide(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a / b

        if (a is Float && b is Float)
            return a / b

        if (a is Int && b is Int)
            return a / b

        if (a is Long && b is Long)
            return a / b

        if (a is BigDecimal && b is BigDecimal)
            return a / b

        if (a is BigInteger && b is BigInteger)
            return a / b

        throw IllegalArgumentException("divide(a,b) is not implemented for this kind of value")
    }

    override fun pow(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a.pow(b)

        if (a is Float && b is Float)
            return a.pow(b)

        if (a is Int && b is Int)
            return a.toDouble().pow(b)

        if (a is Long && b is Long)
            return a.toDouble().pow(b.toInt())

        if (a is BigDecimal && b is BigDecimal)
            return a.toDouble().pow(b.toDouble())

        if (a is BigInteger && b is BigInteger)
            return a.toDouble().pow(b.toInt())

        throw IllegalArgumentException("pow(a,b) is not implemented for this kind of value")
    }

    override fun factorial(n: Any?): Any?
    {
        if (n is Number)
        {
            var s = 1
            for (i in 2..n.toInt())
                s *= i
            return s
        }

        throw IllegalArgumentException("factorial(n) is not implemented for this kind of value")
    }

    override fun mod(a: Any?, b: Any?): Any?
    {
        if (a is Double && b is Double)
            return a.mod(b)

        if (a is Float && b is Float)
            return a.mod(b)

        if (a is Int && b is Int)
            return a.mod(b)

        if (a is Long && b is Long)
            return a.mod(b)

        if (a is BigDecimal && b is BigDecimal)
            return a.toDouble().mod(b.toDouble())

        if (a is BigInteger && b is BigInteger)
            return a.toLong().mod(b.toLong())

        throw IllegalArgumentException("mod(a,b) is not implemented for this kind of value")
    }

    //endregion

    // region BitwiseProtocol

    override fun bitShiftLeft(a: Any?, b: Any?): Any?
    {
        throw IllegalArgumentException("bitShiftLeft(a,b) is not implemented for this kind of value")
    }

    override fun bitShiftRight(a: Any?, b: Any?): Any?
    {
        throw IllegalArgumentException("bitShiftRight(a,b) is not implemented for this kind of value")
    }

    override fun bitAnd(a: Any?, b: Any?): Any?
    {
        throw IllegalArgumentException("bitAnd(a,b) is not implemented for this kind of value")
    }

    override fun bitXor(a: Any?, b: Any?): Any?
    {
        throw IllegalArgumentException("bitXor(a,b) is not implemented for this kind of value")
    }

    override fun bitOr(a: Any?, b: Any?): Any?
    {
        throw IllegalArgumentException("bitOr(a,b) is not implemented for this kind of value")
    }

    //endregion

    //region ConversionProtocol

    override fun convertToNumber(value: Any?): Any?
    {
        if (value is Double)
            return value

        if (value == null)
            return convertToNumber(0.0)

        if (value is String)
            return (StringConversion.optionallyConvertStringToNumber(value, locale = autoParseNumericStringsLocale) as? Number)?.toDouble()
                    ?: 0.0

        return (StringConversion.optionallyConvertStringToNumber(value, locale = autoParseNumericStringsLocale) as? Number)?.toDouble()
                ?: 0.0
    }

    //endregion
}
