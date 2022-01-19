package com.dg.eval

import com.dg.eval.configuration.EvalConfiguration
import com.dg.eval.configuration.EvalFunctionBlock

class CompiledExpression internal constructor(
        internal var root: Token,
        var configuration: EvalConfiguration
)
{
    @Suppress("unused")
    fun execute() : Any? =
            Evaluator.execute(expression = this)

    @Suppress("unused")
    fun setConstant(name: String, value: Any?) {
        configuration.setConstant(name = name, value = value)
    }

    @Suppress("unused")
    fun removeConstant(name: String) {
        configuration.removeConstant(name = name)
    }

    @Suppress("unused")
    fun setFunction(name: String, func: EvalFunctionBlock) {
        configuration.setFunction(name = name, func = func)
    }

    @Suppress("unused")
    fun removeFunction(name: String) {
        configuration.removeFunction(name = name)
    }

    @Suppress("unused")
    fun clearConstants() {
        configuration.clearConstants()
    }

    @Suppress("unused")
    fun clearFunctions() {
        configuration.clearConstants()
    }
}