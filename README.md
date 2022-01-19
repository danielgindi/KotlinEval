Eval for Kotlin
===============

Easily evaluate simple expressions on the go...

This is a port of the [BigEval.js](https://github.com/aviaryan/BigEval.js)/[Eval.net](https://github.com/danielgindi/Eval.net)/[SwiftEval](https://github.com/danielgindi/SwiftEval) library

Features:
* Evaluate basic math operators (`5 * (4 / 3)`)
* Use constants (`x * 27 / 4`)
* Support for pre-defined function calls (`30 * pow(24, 6) / cos(20)`)
* Support for custom function calls
* Support for logic operators (`26 * 3 < 100` - returns a `bool` value)
* Support for bitwise operators (`(1 << 2) == 4`)
* Support for string values (`"test" + 5 == "test5"`)
* Customize the type that is used for numeric values in the expression.
* Customize the code behind the execution of any of the operators.
* Support for compiling an expression and running multiple times while supplying different constants

### Dependency

[Download from Maven Central (.jar)](https://oss.sonatype.org/index.html#view-repositories;releases~browsestorage~/com/github/danielgindi/eval/1.0.1/helpers-1.0.1.jar)

**or**

```java
	dependencies {
    	compile 'com.github.danielgindi:eval:1.0.1'
	}
```

### Usage

```
import com.dg.eval
import com.dg.eval.configuration

val config = DoubleEvalConfiguration()

val result1 = Evaluator.execute("12+45*10", config) as? Double
val result2 = Evaluator.execute("30 * pow(24, 6) / cos(20)", config) as? Double

val compiled = Evaluator.compile("5 * n", config.clone())

compiled.setConstant("n", 8)
val result3 = compiled.execute() as? Double

compiled.setConstant("n", 9)
val result4 = compiled.execute() as? Double

```

### Operators

The operators currently supported in order of precedence are -
```js
[
    ['!'],  // Factorial
    ['**'],  // power
    ['/', '*', '%'],
    ['+', '-'],
    ['<<', '>>'],  // bit shifts
    ['<', '<=', '>', '>='],
    ['==', '=', '!='],   // equality comparisons
    ['&'], ['^'], ['|'],   // bitwise operations
    ['&&'], ['||']   // logical operations
]
```

## Me
* Hi! I am Daniel.
* danielgindi@gmail.com is my email address.
* That's all you need to know.

## Help

If you want to buy me a beer, you are very welcome to
[![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=8VJRAFLX66N54)
 Thanks :-)

## License

This library is under the Apache License 2.0.

This library is free and can be used in commercial applications without royalty.
