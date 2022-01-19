package com.dg.eval.configuration

interface LogicalOperations
{
    fun isTruthy(a: Any?) : Boolean
    fun logicalNot(a: Any?) : Boolean
}