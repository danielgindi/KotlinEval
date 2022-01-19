package com.dg.eval.configuration

import com.dg.eval.StringConversion
import java.lang.Math.pow
import java.security.InvalidParameterException
import java.util.*
import kotlin.math.*

object Defaults
{
    /// <summary>
    /// Ordering of operators
    /// https://en.wikipedia.org/wiki/Order_of_operations#Programming_languages
    /// </summary>
    val defaultOperatorOrder: List<List<String>> = listOf(
            listOf("!"),
            listOf("**"),
            listOf("\\", "/", "*", "%"),
            listOf("+", "-"),
            listOf("<<", ">>"),
            listOf("<", "<=", ">", ">="),
            listOf("==", "=", "!=", "<>"),
            listOf("&"),
            listOf("^"),
            listOf("|"),
            listOf("&&"),
            listOf("||")
    )
    val defaultPrefixOperators: Set<String> = setOf("!")
    val defaultSuffixOperators: Set<String> = setOf("!")
    val defaultRightAssociativeOps: Set<String> = setOf("**")
    val defaultGenericConstants: Map<String, Any?> = mapOf(
            "PI" to Math.PI,
            "PI_2" to Math.PI / 2.0,
            "LOG2E" to log2(Math.E),
            "DEG" to Math.PI / 180.0,
            "E" to Math.E,
            "INFINITY" to Double.POSITIVE_INFINITY,
            "NAN" to Double.NaN,
            "TRUE" to true,
            "FALSE" to false
    )
    val defaultVarNameChars: Set<Char> = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$".toCharArray().toSet()

    fun getDefaultGenericFunctions(
            autoParseNumericStrings: Boolean = true,
            autoParseNumericStringsLocale: Locale? = null): Map<String, EvalFunctionBlock>
    {
        val argFilter: (arg: Any?) -> Any? = if (autoParseNumericStrings)
        {
            { StringConversion.optionallyConvertStringToNumber(it, locale = autoParseNumericStringsLocale) }
        } else
        {
            { it }
        }

        return mapOf(
                "ABS" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    abs(arg)
                },
                "ACOS" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    acos(arg)
                },
                "ASIN" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    asin(arg)
                },
                "ATAN" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    atan(arg)
                },
                "ATAN2" to { args ->
                    val arg1 = argFilter(args[0]) as? Double
                    val arg2 = argFilter(args[1]) as? Double
                    if (args.size != 2 || arg1 == null || arg2 == null)
                    {
                        throw InvalidParameterException()
                    }
                    atan2(arg1, arg2)
                },
                "CEILING" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    ceil(arg)
                },
                "COS" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    cos(arg)
                },
                "COSH" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    cosh(arg)
                },
                "EXP" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    exp(arg)
                },
                "FLOOR" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    floor(arg)
                },
                "LOG" to fn@{ args ->
                    if (args.size == 2)
                    {
                        val arg1 = argFilter(args[0]) as? Double
                        val arg2 = argFilter(args[1]) as? Double
                        if (arg1 == null || arg2 == null)
                            throw InvalidParameterException()

                        return@fn log(arg1, arg2)
                    } else if (args.size == 1)
                    {
                        val arg = argFilter(args[0]) as? Double
                                ?: throw InvalidParameterException()
                        return@fn ln(arg)
                    }
                    throw InvalidParameterException()
                },
                "LOG2" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    log2(arg)
                },
                "LOG10" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                    {
                        throw InvalidParameterException()
                    }
                    log10(arg)
                },
                "MAX" to fn@{ args ->
                    if (args.isEmpty())
                        return@fn null

                    var v = argFilter(args[0]) as? Double ?: return@fn null
                    for (arg in args)
                    {
                        val narg = argFilter(arg) as? Double ?: return@fn null
                        if (narg > v)
                            v = narg
                    }
                    v
                },
                "MIN" to fn@{ args ->
                    if (args.isEmpty())
                        return@fn null

                    var v = argFilter(args[0]) as? Double ?: return@fn null
                    for (arg in args)
                    {
                        val narg = argFilter(arg) as? Double ?: return@fn null
                        if (narg < v)
                            v = narg
                    }
                    v
                },
                "POW" to fn@{ args ->
                    if (args.size == 2)
                    {
                        val arg1 = argFilter(args[0]) as? Double
                        val arg2 = argFilter(args[1]) as? Double
                        if (arg1 == null || arg2 == null)
                            throw InvalidParameterException()

                        return@fn pow(arg1, arg2)
                    }
                    throw InvalidParameterException()
                },
                "ROUND" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    round(arg)
                },
                "SIGN" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    if (arg < 0) -1 else if (arg > 0) 1 else 0
                },
                "SIN" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    sin(arg)
                },
                "SINH" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    sinh(arg)
                },
                "SQRT" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    sqrt(arg)
                },
                "TAN" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    tan(arg)
                },
                "TANH" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    tanh(arg)
                },
                "TRUNCATE" to { args ->
                    val arg = argFilter(args[0]) as? Double
                    if (args.isEmpty() || arg == null)
                        throw InvalidParameterException()

                    truncate(arg)
                })
    }
}
