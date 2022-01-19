package com.dg.eval

import com.dg.eval.configuration.EvalConfiguration
import java.text.ParseException

object Evaluator
{
    @Suppress("MemberVisibilityCanBePrivate")
    fun compile(expression: String, configuration: EvalConfiguration): CompiledExpression
    {
        val tokens = tokenizeExpression(expression = expression, configuration = configuration)
        var end = tokens.size

        // Collapse +-
        var i = 1
        while (i < end)
        {
            val token = tokens[i]
            val prevToken = tokens[i - 1]
            if (token.type == TokenType.Op && (token.value == "-" || token.value == "+") && prevToken.type == TokenType.Op && (prevToken.value == "-" || prevToken.value == "+"))
            {
                if (prevToken.value != "+")
                {
                    if ((token.value == "-"))
                    {
                        token.value = "+"
                    }
                    else
                    {
                        token.value = "-"
                    }
                }
                tokens.removeAt(i - 1)
                end = tokens.size
                continue
            }
            if (token.type == TokenType.Number &&
                    prevToken.type == TokenType.Op &&
                    (prevToken.value == "-" || prevToken.value == "+") &&
                    ((i > 1 && tokens[i - 2].type == TokenType.Op) || i == 1))
            {
                if (prevToken.value == "-")
                {
                    token.value = prevToken.value!! + token.value!!
                }
                tokens.removeAt(i - 1)
                end = tokens.size
                continue
            }
            i += 1
        }

        // Take care of groups (including function calls)
        i = 0
        while (i < end)
        {
            val token = tokens[i]
            if (token.type == TokenType.LeftParen)
            {
                groupTokens(tokens = tokens, startAt = i)
                end = tokens.size
                continue
            }
            i += 1
        }

        // Build the tree
        val tree = buildTree(tokens = tokens, configuration = configuration)

        return CompiledExpression(root = tree, configuration = configuration)
    }

    fun execute(expression: String, configuration: EvalConfiguration): Any? =
            execute(expression = compile(expression = expression, configuration = configuration))

    fun execute(expression: CompiledExpression): Any? =
            evaluateToken(token = expression.root, configuration = expression.configuration)

    private fun opAtPosition(expression: String, start: Int, configuration: EvalConfiguration): String?
    {
        var op: String? = null
        val allOperators = configuration.allOperators
        for (item in allOperators)
        {
            if (op != null && (op == item || item.length <= op.length))
            {
                continue
            }
            if (expression.substring(
                            startIndex = start,
                            endIndex = start + item.length) == item)
            {
                op = item
            }
        }
        return op
    }

    private fun indexOfOpInTokens(tokens: List<Token>, op: String): Int?
    {
        for ((i, token) in tokens.withIndex())
        {
            if (token.type == TokenType.Op && token.value == op)
            {
                return i
            }
        }
        return null
    }

    private fun lastIndexOfOpInTokens(tokens: List<Token>, op: String): Int?
    {
        for (i in tokens.size - 1 downTo 0)
        {
            val token = tokens[i]
            if (token.type == TokenType.Op && token.value == op)
            {
                return i
            }
        }
        return null
    }

    private data class MatchWithIndex(val index: Int, val match: String)

    private fun lastIndexOfOpArray(tokens: List<Token>,
                                   ops: List<String>,
                                   config: EvalConfiguration): MatchWithIndex?
    {
        var pos: Int? = null
        var bestMatch: String? = null

        for (item in ops)
        {
            val opIndex = (if (config.rightAssociativeOps.contains(item))
            {
                indexOfOpInTokens(tokens = tokens, op = item)
            }
            else
            {
                lastIndexOfOpInTokens(tokens = tokens, op = item)
            }) ?: continue

            if (pos == null || opIndex > pos)
            {
                pos = opIndex
                bestMatch = item
            }
        }

        if (pos == null || bestMatch == null)
            return null

        return MatchWithIndex(index = pos, match = bestMatch)
    }

    private fun parseString(data: String, startAt: Int,
                            @Suppress("SameParameterValue") strict: Boolean,
                            @Suppress("SameParameterValue") unquote: Boolean): MatchWithIndex
    {
        var i = startAt
        val endIndex = data.length
        var output = ""
        var quote = '\u0000'
        if (unquote)
        {
            quote = data[i]

            if (quote != '\'' && quote != '\"')
                throw ParseException("Not a string", i)

            i += 1
        }

        while (i < endIndex)
        {
            var c = data[i]
            if (c == '\\')
            {
                if (i + 1 == endIndex)
                    throw ParseException("Invalid string. An escape character with no escapee encountered at index $i", i)

                // Take a step forward here
                i += 1
                c = data[i]
                // Test escapee
                if (c == '\\' || c == '\'' || c == '\"')
                {
                    output += c
                }
                else if (c == 'b')
                {
                    output += '\u0008'
                }
                else if (c == 'f')
                {
                    output += '\u000c'
                }
                else if (c == 'n')
                {
                    output += '\n'
                }
                else if (c == 'r')
                {
                    output += '\r'
                }
                else if (c == 't')
                {
                    output += '\t'
                }
                else if (c == 'u' || c == 'x')
                {
                    var uffff = 0
                    val hexSize = if (c == 'u') 4 else 2
                    for (j in 0 until hexSize)
                    {
                        i += 1
                        c = data[i]
                        var hex: Int
                        if (c in '0'..'9')
                        {
                            hex = c - '0'
                        }
                        else if (c in 'a'..'f')
                        {
                            hex = c - 'a' + 10
                        }
                        else if (c in 'A'..'F')
                        {
                            hex = c - 'A' + 10
                        }
                        else
                        {
                            if ((strict))
                            {
                                throw ParseException("Unexpected escape sequence at index ${i - j - 2}", i - j - 2)
                            }
                            else
                            {
                                i -= 1
                                break
                            }
                        }
                        uffff = uffff * 16 + hex
                    }
                    output += Char(uffff)
                }
                else
                {
                    if (strict)
                    {
                        throw ParseException("Unexpected escape sequence at index ${i - 1}", i - 1)
                    }
                    else
                    {
                        output += c
                    }
                }
            }
            else if (unquote && c == quote)
            {
                return MatchWithIndex(index = i + 1, match = output)
            }
            else
            {
                output += c
            }
            i++
        }

        if (unquote)
            throw ParseException("String must be quoted with matching single-quote (') or double-quote(\") characters.", i)

        return MatchWithIndex(index = i, match = output)
    }

    private fun parseNumber(data: String, startAt: Int): MatchWithIndex
    {
        var i = startAt
        val endIndex = data.length
        var exp = 0
        var dec = false
        if (i >= endIndex)
        {
            throw ParseException("Can't parse token at $i", i)
        }
        while (i < endIndex)
        {
            val c = data[i]
            if ((c in '0'..'9'))
            {
                if (exp == 1 || exp == 2)
                {
                    exp = 3
                }
            }
            else if ((c == '.'))
            {
                if (dec || exp > 0)
                {
                    break
                }
                dec = true
            }
            else if ((c == 'e'))
            {
                if (exp > 0)
                {
                    break
                }
                exp = 1
            }
            else if ((exp == 1 && (c == '-' || c == '+')))
            {
                exp = 2
            }
            else
            {
                break
            }
            i++
        }
        if ((i == startAt || exp == 1 || exp == 2))
        {
            throw ParseException("Unexpected character at index $i", i)
        }

        return MatchWithIndex(
                index = i,
                match = data.substring(startIndex = startAt, endIndex = i))
    }

    private fun tokenizeExpression(expression: String, configuration: EvalConfiguration): MutableList<Token>
    {
        val varNameChars = configuration.varNameChars
        val tokens = mutableListOf<Token>()

        if (expression.isEmpty())
            return tokens

        var i = 0
        val endIndex = expression.length
        while (i < endIndex)
        {
            var c = expression[i]

            val isDigit = c in '0'..'9'

            if (isDigit || c == '.')
            {
                // Starting a number
                val parsedNumber = parseNumber(data = expression, startAt = i)
                tokens.add(Token(type = TokenType.Number, position = i, value = parsedNumber.match))
                i = parsedNumber.index
                continue
            }

            var isVarChars = varNameChars.contains(c)
            if (isVarChars)
            {
                // Starting a variable name - can start only with A-Z_
                var token = ""
                while (i < endIndex)
                {
                    c = expression[i]
                    isVarChars = varNameChars.contains(c)
                    if (!isVarChars)
                    {
                        break
                    }
                    token += c
                    i++
                }
                tokens.add(Token(
                        type = TokenType.Var,
                        position = i - token.length,
                        value = token
                ))
                continue
            }

            if (c == '\'' || c == '\"')
            {
                val parsedString = parseString(data = expression, startAt = i, strict = false, unquote = true)
                tokens.add(Token(type = TokenType.String, position = i, value = parsedString.match))
                i = parsedString.index
                continue
            }

            if (c == '(')
            {
                tokens.add(Token(type = TokenType.LeftParen, position = i))
                i++
                continue
            }

            if (c == ')')
            {
                tokens.add(Token(type = TokenType.RightParen, position = i))
                i++
                continue
            }

            if (c == ',')
            {
                tokens.add(Token(type = TokenType.Comma, position = i))
                i++
                continue
            }
            if (c == ' ' || c == '\t' || c == '\u000c' || c == '\r' || c == '\n')
            {
                // Whitespace, skip
                i++
                continue
            }

            val op = opAtPosition(expression = expression, start = i, configuration = configuration)
            if (op != null)
            {
                tokens.add(Token(type = TokenType.Op, position = i, value = op))
                i += op.length
                continue
            }

            throw ParseException("Unexpected token at index $i", i)
        }
        return tokens
    }

    private fun groupTokens(tokens: MutableList<Token>, startAt: Int = 0): Token
    {
        val isFunc = startAt > 0 && tokens[startAt - 1].type == TokenType.Var
        val rootToken = tokens[if (isFunc) startAt - 1 else startAt]

        var groups: MutableList<MutableList<Token>>? = null
        var sub: MutableList<Token> = mutableListOf()
        if (isFunc)
        {
            rootToken.type = TokenType.Call
            groups = mutableListOf()
            rootToken.argumentsGroups = groups
        }
        else
        {
            rootToken.type = TokenType.Group
            rootToken.tokens = sub
        }

        var i = startAt + 1
        var end = tokens.size

        while (i < end)
        {
            val token = tokens[i]

            if (isFunc && token.type == TokenType.Comma)
            {
                sub = mutableListOf()
                groups!!.add(sub)
                i += 1
                continue
            }

            if (token.type == TokenType.RightParen)
            {
                if (isFunc)
                {
                    for (r in i downTo startAt)
                        tokens.removeAt(r)
                }
                else
                {
                    for (r in i downTo (startAt + 1))
                        tokens.removeAt(r)
                }
                return rootToken
            }

            if (token.type == TokenType.LeftParen)
            {
                groupTokens(tokens = tokens, startAt = i)
                end = tokens.size
                continue
            }

            if (isFunc && groups!!.size == 0)
            {
                groups.add(sub)
            }

            sub.add(token)
            i += 1
        }

        throw ParseException("Unmatched parenthesis for parenthesis at index ${tokens[startAt]}", startAt)
    }

    private fun buildTree(tokens: MutableList<Token>, configuration: EvalConfiguration): Token
    {
        val order = configuration.operatorOrder
        val orderCount = order.size
        val prefixOps = configuration.prefixOperators
        val suffixOps = configuration.suffixOperators
        var i = orderCount - 1
        while (i >= 0)
        {
            val cs = order[i]
            val match = lastIndexOfOpArray(tokens = tokens, ops = cs, config = configuration)
            if (match == null)
            {
                i -= 1
                continue
            }

            val pos = match.index
            val op = match.match

            val token = tokens[pos]
            var left: MutableList<Token>?
            var right: MutableList<Token>?

            if (prefixOps.contains(op) || suffixOps.contains(op))
            {
                left = null
                right = null
                if (prefixOps.contains(op) && pos == 0)
                {
                    right = tokens.subList(pos + 1, tokens.size)
                }
                else if (suffixOps.contains(op) && pos > 0)
                {
                    left = tokens.subList(0, pos)
                }
                if (left == null && right == null)
                {
                    throw ParseException("Operator ${token.value ?: "(null)"} is unexpected at index ${token.position}", token.position
                            ?: pos)
                }
            }
            else
            {
                left = tokens.subList(0, pos)
                right = tokens.subList(pos + 1, tokens.size)
                if (left.size == 0 && (op == "-" || op == "+"))
                {
                    left = null
                }
            }
            if ((left != null && left.size == 0) || (right != null && right.size == 0))
            {
                throw ParseException("Invalid expression, missing operand", pos)
            }
            if (left == null && op == "-")
            {
                left = mutableListOf()
                left.add(Token(type = TokenType.Number, value = "0"))
            }
            else if (left == null && op == "+")
            {
                return buildTree(tokens = right!!, configuration = configuration)
            }
            if (left != null)
            {
                token.left = buildTree(tokens = left, configuration = configuration)
            }
            if (right != null)
            {
                token.right = buildTree(tokens = right, configuration = configuration)
            }
            return token
        }

        if (tokens.size > 1)
        {
            throw ParseException("Invalid expression, missing operand or operator at ${tokens[1].position}", tokens[1].position
                    ?: -1)
        }

        if (tokens.size == 0)
        {
            throw ParseException("Invalid expression, missing operand or operator.", -1)
        }

        var singleToken = tokens[0]

        when (singleToken.type)
        {
            TokenType.Group ->
            {
                singleToken = buildTree(
                        tokens = singleToken.tokens!!,
                        configuration = configuration)
            }

            TokenType.Call ->
            {
                singleToken.arguments = mutableListOf()
                for (a in 0 until (singleToken.argumentsGroups?.size ?: 0))
                {
                    if (singleToken.argumentsGroups!![a].size == 0)
                    {
                        singleToken.arguments?.add(null)
                    }
                    else
                    {
                        singleToken.arguments?.add(
                                buildTree(tokens = singleToken.argumentsGroups!![a],
                                        configuration = configuration))
                    }
                }
            }

            TokenType.Comma ->
            {
                throw ParseException("Unexpected character at index ${singleToken.position}",
                        singleToken.position ?: -1)
            }
            else ->
            {
            }
        }

        return singleToken
    }

    private fun evaluateToken(token: Token, configuration: EvalConfiguration): Any?
    {
        val tokenValue = token.value

        when (token.type)
        {
            TokenType.String -> return tokenValue
            TokenType.Number ->
            {
                if (tokenValue == null)
                    return null
                return configuration.convertToNumber(tokenValue)
            }
            TokenType.Var ->
            {
                if (tokenValue == null)
                    return null

                var value = configuration.constProvider?.invoke(tokenValue)
                if (value != null)
                    return value

                val constants = configuration.constants
                if (constants != null)
                {
                    value = constants[tokenValue]
                    if (value != null)
                        return value

                    value = constants[tokenValue.uppercase()]
                    if (value != null)
                        return value
                }

                value = configuration.genericConstants[tokenValue]
                if (value != null)
                    return value

                value = configuration.genericConstants[tokenValue.uppercase()]
                if (value != null)
                    return value

                return null
            }
            TokenType.Call -> return evaluateFunction(token = token, configuration = configuration)
            TokenType.Op ->
            {
                when (token.value)
                {
                    "!" ->
                    {
                        // Factorial or Not
                        if (token.left != null)
                        {
                            // Factorial (i.e. 5!)
                            return configuration.factorial(
                                    evaluateToken(token = token.left!!,
                                            configuration = configuration))
                        }
                        else if (token.right != null)
                        {
                            // Not (i.e. !5)
                            return configuration.logicalNot(
                                    evaluateToken(token = token.right!!,
                                            configuration = configuration))
                        }
                    }
                    else ->
                    {
                        val left = token.left
                        val right = token.right
                        if (left == null || right == null)
                        {
                            throw ParseException("An unexpected error occurred while evaluating expression", -1)
                        }

                        when (token.value)
                        {
                            "/", "\\" -> // Divide
                                return configuration.divide(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "*" -> // Multiply
                                return configuration.multiply(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "+" -> // Add
                                return configuration.add(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "-" -> // Subtract
                                return configuration.subtract(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "<<" -> // Shift left
                                return configuration.bitShiftLeft(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            ">>" -> // Shift right
                                return configuration.bitShiftRight(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "<" -> // Less than
                                return configuration.lessThan(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "<=" -> // Less than or equals to
                                return configuration.lessThanOrEqualsTo(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            ">" -> // Greater than
                                return configuration.greaterThan(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            ">=" -> // Greater than or equals to
                                return configuration.greaterThanOrEqualsTo(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "==", "=" -> // Equals to
                                return configuration.equalsTo(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "!=", "<>" -> // Not equals to
                                return configuration.notEqualsTo(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "**" -> // Power
                                return configuration.pow(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "%" -> // Mod
                                return configuration.mod(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "&" -> // Bitwise AND
                                return configuration.bitAnd(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "^" -> // Bitwise XOR
                                return configuration.bitXor(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "|" -> // Bitwise OR
                                return configuration.bitOr(
                                        a = evaluateToken(token = left, configuration = configuration),
                                        b = evaluateToken(token = right, configuration = configuration))
                            "&&" ->
                            {
                                // Logical AND
                                val res = evaluateToken(token = left, configuration = configuration)
                                if (configuration.isTruthy(res))
                                    return evaluateToken(token = right, configuration = configuration)
                                return res
                            }
                            "||" ->
                            {
                                // Logical OR
                                val res = evaluateToken(token = left, configuration = configuration)
                                if (configuration.logicalNot(res))
                                    return evaluateToken(token = right, configuration = configuration)
                                return res
                            }
                        }
                    }
                }
            }
            TokenType.Group ->
            {
            }
            TokenType.LeftParen ->
            {
            }
            TokenType.RightParen ->
            {
            }
            TokenType.Comma ->
            {
            }
        }

        throw ParseException("An unexpected error occurred while evaluating expression", -1)
    }

    private fun evaluateFunction(token: Token, configuration: EvalConfiguration): Any
    {
        val fname = token.value ?: ""

        val args = mutableListOf<Any?>()
        for (arg in token.arguments ?: listOf())
        {
            if (arg == null)
            {
                args.add(null)
            }
            else
            {
                args.add(evaluateToken(token = arg, configuration = configuration))
            }
        }

        var value: Any?

        val functions = configuration.functions
        if (functions != null)
        {
            value = functions[fname]?.invoke(args)
            if (value != null)
                return value

            value = functions[fname.uppercase()]?.invoke(args)
            if (value != null)
                return value
        }

        value = configuration.genericFunctions[fname]?.invoke(args)
        if (value != null)
            return value

        value = configuration.genericFunctions[fname.uppercase()]?.invoke(args)
        if (value != null)
            return value

        throw ParseException("Function named \"${fname}\" was not found", -1)
    }
}
