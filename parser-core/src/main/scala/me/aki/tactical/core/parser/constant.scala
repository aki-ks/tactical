package me.aki.tactical.core.parser

import fastparse.all._
import me.aki.tactical.core.constant._

object IntConstantParser extends Parser[IntConstant] {
  val parser: P[IntConstant] =
    for (int ← int) yield new IntConstant(int.toInt)
}

object LongConstantParser extends Parser[LongConstant] {
  val parser: P[LongConstant] =
    for (long ← long ~ ("l" | "L")) yield new LongConstant(long.toLong)
}

object FloatConstantParser extends Parser[FloatConstant] {
  val parser: P[FloatConstant] =
    for (float ← float ~ ("f" | "F")) yield new FloatConstant(float.toFloat)
}

object DoubleConstantParser extends Parser[DoubleConstant] {
  val parser: P[DoubleConstant] =
    for (double ← double ~ ("d" | "D")) yield new DoubleConstant(double.toDouble)
}

object StringConstantParser extends Parser[StringConstant] {
  val parser: P[StringConstant] =
    for (string ← StringLiteral) yield new StringConstant(string)
}

object FieldConstantParser extends Parser[FieldConstant] {
  val parser: P[FieldConstant] = P {
    StringConstantParser |
    FloatConstantParser |
    DoubleConstantParser |
    LongConstantParser |
    IntConstantParser
  }
}
