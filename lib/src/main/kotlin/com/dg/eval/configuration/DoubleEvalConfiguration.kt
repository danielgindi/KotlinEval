@file:Suppress("unused")

package com.dg.eval.configuration

import com.dg.eval.StringConversion
import java.text.NumberFormat
import java.util.*
import kotlin.math.pow

open class DoubleEvalConfiguration(
        populateDefaults: Boolean = true,
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
        constProvider: ConstProvider? = null
) : EvalConfiguration(
        populateDefaults,
        autoParseNumericStrings, autoParseNumericStringsLocale,
        operatorOrder, prefixOperators, suffixOperators, rightAssociativeOps,
        varNameChars,
        genericConstants, genericFunctions, constants, functions,
        constProvider)
{
    override fun clone(deep: Boolean): DoubleEvalConfiguration
    {
        return DoubleEvalConfiguration(
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

    private fun filterArg(arg: Any?): Any?
    {
        if (autoParseNumericStrings)
            return StringConversion.optionallyConvertStringToNumber(arg, locale = autoParseNumericStringsLocale)

        return arg
    }

    private val stringifyDoubleFormatter: NumberFormat = run {
        val formatter = NumberFormat.getNumberInstance()
        formatter.minimumFractionDigits = 0
        formatter
    }

    override fun add(a: Any?, b: Any?): Any?
    {
        if (a is String)
        {
            // do not convert them to numbers by mistake.
            // multiplication could be used to cast to numbers.
            return if (b is Double)
            {
                a + (stringifyDoubleFormatter.format(b) ?: "$b")
            } else
            {
                a + (if (b == null) "" else "$b")
            }
        }

        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.add(a, b)

        return na + nb
    }

    override fun subtract(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.subtract(a, b)

        return na - nb
    }

    override fun multiply(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.multiply(a, b)

        return na * nb
    }

    override fun divide(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.divide(a, b)

        return na / nb
    }

    override fun pow(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.pow(a, b)

        return na.pow(nb)
    }

    override fun factorial(n: Any?): Any?
    {
        val nn = filterArg(n) as? Double ?: return super.factorial(n)
        var s = 1
        for (i in 2..nn.toInt())
            s *= i
        return s
    }

    override fun mod(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.mod(a, b)

        return na.mod(nb)
    }

    override fun bitShiftLeft(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.bitShiftLeft(a, b)

        return na.toLong().shl(nb.toInt()).toDouble()
    }

    override fun bitShiftRight(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.bitShiftLeft(a, b)

        return na.toLong().shr(nb.toInt()).toDouble()
    }

    override fun bitAnd(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.bitShiftLeft(a, b)

        return na.toLong().and(nb.toLong()).toDouble()
    }

    override fun bitXor(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.bitShiftLeft(a, b)

        return na.toLong().xor(nb.toLong()).toDouble()
    }

    override fun bitOr(a: Any?, b: Any?): Any?
    {
        val na = filterArg(a) as? Double
        val nb = filterArg(b) as? Double
        if (na == null || nb == null)
            return super.bitShiftLeft(a, b)

        return na.toLong().or(nb.toLong()).toDouble()
    }
}
