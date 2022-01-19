package com.dg.eval

class Token {
    constructor(type: TokenType, position: Int) {
        this.type = type
        this.position = position
    }

    constructor(type: TokenType, position: Int, value: String?) {
        this.type = type
        this.position = position
        this.value = value
    }

    constructor(type: TokenType, value: String?) {
        this.type = type
        this.value = value
    }

    var type: TokenType
    var value: String? = null
    var position: Int? = null
    var argumentsGroups: MutableList<MutableList<Token>>? = null
    var arguments: MutableList<Token?>? = null
    var tokens: MutableList<Token>? = null
    var left: Token? = null
    var right: Token? = null
}
