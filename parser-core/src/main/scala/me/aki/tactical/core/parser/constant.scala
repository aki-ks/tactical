package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._

import me.aki.tactical.core.`type`.RefType
import me.aki.tactical.core.constant._

object NullConstantParser extends Parser[NullConstant] {
  val parser: P[NullConstant] =
    for (_ ← "null".!) yield NullConstant.getInstance
}

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

object ClassConstantParser extends Parser[ClassConstant] {
  val parser: P[ClassConstant] =
    for (clazz ← ClassLiteral if clazz.isInstanceOf[RefType]) yield new ClassConstant(clazz.asInstanceOf[RefType])
}

object MethodTypeConstantParser extends Parser[MethodTypeConstant] {
  val parser: P[MethodTypeConstant] =
    for (desc ← "method" ~ WS.? ~ "{" ~ WS.? ~ MethodDescriptorParser ~ WS.? ~ "}") yield new MethodTypeConstant(desc.getParameterTypes, desc.getReturnType)
}

object HandleConstantParser extends Parser[HandleConstant] {
  val parser: P[HandleConstant] =
    for (handle ← "handle" ~ WS.? ~ "{" ~ WS.? ~ HandleParser ~ WS.? ~ "}") yield new HandleConstant(handle)
}

object DynamicConstantParser extends Parser[DynamicConstant] {
  val parser: P[DynamicConstant] = P {
    val arguments = "[" ~ WS.? ~ BootstrapConstantParser.rep(min = 0, sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ "]"

    for {
      (name, typ, bootstrap, arguments) ←
        "dynamic" ~ WS.? ~ "{" ~ WS.? ~
          "name" ~ WS.? ~ "=" ~ WS.? ~ StringLiteral ~ WS.? ~ "," ~ WS.? ~
          "type" ~ WS.? ~ "=" ~ WS.? ~ TypeParser ~ WS.? ~ "," ~ WS.? ~
          "bootstrap" ~ WS.? ~ "=" ~ WS.? ~ HandleParser ~ WS.? ~
          ("," ~ WS.? ~ "arguments" ~ WS.? ~ "=" ~ WS.? ~ arguments ~ WS.?).? ~
        "}"
    } yield new DynamicConstant(name, typ, bootstrap, arguments.getOrElse(Nil).asJava)
  }
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

object PushableConstantParser extends Parser[PushableConstant] {
  val parser: P[PushableConstant] = P {
    NullConstantParser |
    FloatConstantParser |
    DoubleConstantParser |
    LongConstantParser |
    IntConstantParser |
    StringConstantParser |
    ClassConstantParser |
    DynamicConstantParser |
    HandleConstantParser |
    MethodTypeConstantParser
  }
}

object BootstrapConstantParser extends Parser[BootstrapConstant] {
  val parser: P[BootstrapConstant] = P {
    DoubleConstantParser |
      FloatConstantParser |
      LongConstantParser |
      IntConstantParser |
      StringConstantParser |
      ClassConstantParser |
      HandleConstantParser |
      MethodTypeConstantParser
  }
}
