package com.dg.eval.configuration

interface ComparisonOperations
{
    fun compare(a: Any?, b: Any?) : Int?
    fun lessThan(a: Any?, b: Any?) : Boolean
    fun lessThanOrEqualsTo(a: Any?, b: Any?) : Boolean
    fun greaterThan(a: Any?, b: Any?) : Boolean
    fun greaterThanOrEqualsTo(a: Any?, b: Any?) : Boolean
    fun equalsTo(a: Any?, b: Any?) : Boolean
    fun notEqualsTo(a: Any?, b: Any?) : Boolean
    fun isTruthy(a: Any?) : Boolean
    fun logicalNot(a: Any?) : Boolean
}