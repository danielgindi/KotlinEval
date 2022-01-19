@file:Suppress("unused")

package com.dg.eval.configuration

typealias EvalFunctionBlock = (args: List<Any?>) -> Any?
typealias ConstProvider = (varname: String) -> Any?
