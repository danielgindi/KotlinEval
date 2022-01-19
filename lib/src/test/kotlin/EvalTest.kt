import com.dg.eval.Evaluator
import com.dg.eval.configuration.DoubleEvalConfiguration
import com.dg.eval.configuration.EvalConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.pow

class EvalTest
{
    @Suppress("SimplifyBooleanWithConstants")
    @Test
    fun basicEvalTests()
    {
        val config = DoubleEvalConfiguration()

        testExpr("12+45*10", value = 12.0 + 45 * 10, config = config)

        val d = 12.0 / 4.0 * 5.0 + 45.0 * 13.0 - 72.0 * 598.0
        testExpr("12/4 * 5 + 45*13 - 72 * 598",
                value = d,
                config = config)

        testExpr("345 / 23 * 124 / 41 * 12",
                value = 345.0 / 23 * 124 / 41 * 12,
                config = config)

        testExpr("345 / 23 >> 3 * 124 / 41 * 12",
                value = ((345.0 / 23).toLong().shr((3.0 * 124.0 / 41.0 * 12.0).toInt())).toDouble(),
                config = config)

        testExpr("345 / (23 >> 3) * 124 / 41 * 12",
                value = 345.0 / ((23).shr(3)).toDouble() * 124.0 / 41.0 * 12.0,
                config = config)

        testExpr("345 / pow(5,12/9) * 124 / 41 * 12",
                value = 345.0 / 5.0.pow(12.0 / 9.0) * 124.0 / 41.0 * 12.0,
                config = config)

        testExpr("-5&&2==7&&-4>=-5>>-8*-5",
                value = (-5 != 0 && 2 == 7 && -4 >= (-5).shr(-8 * -5)),
                config = config)

        testExpr("\"testing\" == \"testing\"",
                value = true,
                config = config)

        testExpr("\"testing\"",
                value = "testing",
                config = config)

        testExpr("\"testing\" + 58.3",
                value = "testing58.3",
                config = config)

        val withConsts = config.clone()
        withConsts.setConstant(name = "x",
                value = 5.9)
        testExpr("x * 27 + (8>>2) / x",
                value = 5.9 * 27.0 + ((8).shr(2)).toDouble() / 5.9,
                config = withConsts)

        testExpr("max(1,5,8.7)",
                value = 8.7, config = withConsts)

        testExpr("30 * PI",
                value = 30 * Math.PI, config = withConsts)

        testExpr("-4^(7**2)**-2",
                value = -4.0, config = config)

        testExpr("-4^7**(2**-2)",
                value = -3.0, config = config)

        testExpr("-4^7**2**-2",
                value = -3.0, config = config)

        testExpr("\"-4\"^7**\"2\"**-2",
                value = -3.0, config = config)

        testExpr("\"abc\"+5", value = "abc5", config = config)

        testExpr("\"5\"+5", value = "55", config = config)

        testExpr("5+\"5\"", value = 10.0, config = config)

        testExpr("12e5", value = 1200000.0, config = config)

        testExpr("12e+5", value = 1200000.0, config = config)
    }

    private fun testExpr(expr: String, value: Double?, config: EvalConfiguration)
    {
        assertEquals(Evaluator.execute(expr, config) as? Double, value)
    }

    private fun testExpr(expr: String, value: Boolean?, config: EvalConfiguration)
    {
        assertEquals(Evaluator.execute(expr, config) as? Boolean, value)
    }

    private fun testExpr(expr: String, value: String?, config: EvalConfiguration)
    {
        assertEquals(Evaluator.execute(expr, config) as? String, value)
    }
}