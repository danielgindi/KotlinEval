package com.dg.eval

import java.text.NumberFormat
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
object StringConversion
{
    fun guessNumberComma(value: String, allowThousands: Boolean): Boolean
    {
        val sval = value.trim()
        val p1 = value.indexOf('.')
        val p2 = if (p1 == -1) null else value.lastIndexOf('.')
        val c1 = sval.indexOf(',')
        val c2 = if (c1 == -1) null else value.lastIndexOf(',')
        val hasSign = value.isNotEmpty() && (sval.startsWith("-") || sval.startsWith("+"))
        val lenNoSign = if (hasSign) value.length - 1 else value.length

        val isCommaBased: Boolean

        if (c1 != -1 && p1 != -1)
        {
            // who's last?
            isCommaBased = c2!! > p2!!
        } else if (c1 != c2)
        {
            // two commas, must be thousands
            isCommaBased = false
        } else if (p1 != p2)
        {
            // two periods, must be thousands
            isCommaBased = true
        } else if (c2 != -1 && (lenNoSign > 7 || lenNoSign < 5))
        {
            // there is a comma, but it could not be thousands as there should be more than one
            isCommaBased = true
        } else if (p2 != -1 && (lenNoSign > 7 || lenNoSign < 5))
        {
            // there is a period, but it could not be thousands as there should be more than one
            isCommaBased = false
        } else if (c1 != -1 && c2 != sval.length - 4)
        {
            // comma not in thousands position
            isCommaBased = true
        } else if (p1 != -1 && p2 != sval.length - 4)
        {
            // period not in thousands position
            isCommaBased = false
        } else
        {
            // if there's a period in the thousands position -> guess that the number is comma based with thousands group
            isCommaBased = allowThousands && p1 != -1
        }
        return isCommaBased
    }

    private val COMMA_BASED_LOCALE: Locale = Locale.forLanguageTag("es")
    private val PERIOD_BASED_LOCALE: Locale = Locale.forLanguageTag("en")

    fun optionallyConvertStringToNumber(value: Any?, locale: Locale? = null): Any?
    {
        val sval = (value as? String)
                ?.replace('e', 'E') // NumberFormatter's exponent is case sensitive
                ?.replace("E+", "E") // NumberFormatter's exponent is neutral or negative
        if (sval != null)
        {
            var formatter: NumberFormat

            if (locale != null)
            {
                formatter = NumberFormat.getNumberInstance(locale)
                formatter.isGroupingUsed = true
            }
            else
            {
                formatter = NumberFormat.getNumberInstance(
                        if (guessNumberComma(value = sval, allowThousands = true))
                            COMMA_BASED_LOCALE else PERIOD_BASED_LOCALE
                )
            }

            var number = formatter.parse(sval)
            if (number != null)
                return number.toDouble()

            if (locale != null) {
                formatter = NumberFormat.getNumberInstance(
                        if (guessNumberComma(value = sval, allowThousands = true))
                            COMMA_BASED_LOCALE else PERIOD_BASED_LOCALE
                )

                number = formatter.parse(sval)
                if (number != null)
                    return number.toDouble()
            }

            return value
        }
        else
        {
            return value
        }
    }
}
