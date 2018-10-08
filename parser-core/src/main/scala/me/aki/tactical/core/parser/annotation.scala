package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.Path
import me.aki.tactical.core.annotation._

object BooleanAnnotationValueParser extends Parser[BooleanAnnotationValue] {
  val parser: P[BooleanAnnotationValue] =
    for (boolean ← boolean)
      yield new BooleanAnnotationValue(boolean)
}

object ByteAnnotationValueParser extends Parser[ByteAnnotationValue] {
  val parser: P[ByteAnnotationValue] =
    for (byte ← integerNumber.! ~ ("b" | "B"))
      yield new ByteAnnotationValue(byte.toByte)
}

object CharAnnotationValueParser extends Parser[CharAnnotationValue] {
  val parser: P[CharAnnotationValue] =
    for (char ← CharLiteral)
      yield new CharAnnotationValue(char)
}

object ShortAnnotationValueParser extends Parser[ShortAnnotationValue] {
  val parser: P[ShortAnnotationValue] =
    for (short ← integerNumber.! ~ ("s" | "S"))
      yield new ShortAnnotationValue(short.toShort)
}

object IntAnnotationValueParser extends Parser[IntAnnotationValue] {
  val parser: P[IntAnnotationValue] =
    for (int ← integerNumber.!)
      yield new IntAnnotationValue(int.toInt)
}

object LongAnnotationValueParser extends Parser[LongAnnotationValue] {
  val parser: P[LongAnnotationValue] =
    for (long ← integerNumber.! ~ ("l" | "L"))
      yield new LongAnnotationValue(long.toLong)
}

object FloatAnnotationValueParser extends Parser[FloatAnnotationValue] {
  val parser: P[FloatAnnotationValue] =
    for (float ← floatingNumber.! ~ ("f" | "F"))
      yield new FloatAnnotationValue(float.toFloat)
}

object DoubleAnnotationValueParser extends Parser[DoubleAnnotationValue] {
  val parser: P[DoubleAnnotationValue] =
    for (double ← floatingNumber.! ~ ("d" | "D"))
      yield new DoubleAnnotationValue(double.toDouble)
}

object PrimitiveAnnotationValueParser extends Parser[PrimitiveAnnotationValue] {
  val parser: P[PrimitiveAnnotationValue] = P {
    BooleanAnnotationValueParser |
      CharAnnotationValueParser |
      ByteAnnotationValueParser |
      ShortAnnotationValueParser |
      LongAnnotationValueParser |
      FloatAnnotationValueParser |
      DoubleAnnotationValueParser |
      IntAnnotationValueParser
  }
}

object StringAnnotationValue extends Parser[StringAnnotationValue] {
  val parser: P[StringAnnotationValue] =
    for (string ← StringLiteral) yield new StringAnnotationValue(string)
}

object ArrayAnnotationValueParser extends Parser[ArrayAnnotationValue] {
  val parser: P[ArrayAnnotationValue] = P {
    val values = AnnotationValueParser.rep(min = 0, sep = WS.? ~ "," ~ WS.?)

    for (array ← "{" ~ WS.? ~ values ~ WS.? ~ "}")
      yield new ArrayAnnotationValue(array.asJava)
  }
}

object ClassAnnotationValue extends Parser[ClassAnnotationValue] {
  val parser: P[ClassAnnotationValue] =
    for (clazz ← ClassLiteral)
      yield new ClassAnnotationValue(clazz)
}

object EnumAnnotationValue extends Parser[EnumAnnotationValue] {
  val parser: P[EnumAnnotationValue] =
    for (strings ← Literal.rep(min = 2, sep = ".")) yield {
      val field = strings.last
      val owner = strings.init
      new EnumAnnotationValue(new Path(owner.init.asJava, owner.last), field)
    }
}

object AnnotationAnnotationValueParser extends Parser[AnnotationAnnotationValue] {
  val parser: P[AnnotationAnnotationValue] = {
    for ((path, values) ← "@" ~ WS.? ~ PathParser ~ WS.? ~ "(" ~ WS.? ~ AnnotationParser.values ~ WS.? ~ ")")
      yield new AnnotationAnnotationValue(path, values)
  }
}

object AnnotationValueParser extends Parser[AnnotationValue] {
  val parser: P[AnnotationValue] = P {
    AnnotationAnnotationValueParser |
    ArrayAnnotationValueParser |
    StringAnnotationValue |
    PrimitiveAnnotationValueParser |
    ClassAnnotationValue |
    EnumAnnotationValue
  }
}

object AnnotationParser extends Parser[Annotation] {
  private def toLinkedHashMap(values: Seq[(String, AnnotationValue)]) = {
    val valueMap = new java.util.LinkedHashMap[String, AnnotationValue]()
    for ((key, value) ← values) valueMap.put(key, value)
    valueMap
  }

  val values = P {
    val value = Literal ~ WS.? ~ "=" ~ WS.? ~ AnnotationValueParser
    value.rep(min = 0, sep = WS.? ~ "," ~ WS.?)
  } map toLinkedHashMap

  val parser: P[Annotation] = P {
    val annotationClass = P { "@" ~ WS.? ~ PathParser }
    val visibility = P { "[" ~ WS.? ~ "visible" ~ WS.? ~ "=" ~ WS.? ~ boolean ~ WS.? ~ "]" }
    val valueList = P { "(" ~ WS.? ~ AnnotationParser.values ~ WS.? ~ ")" }

    for ((path, isVisible, values) ← annotationClass ~ WS.? ~ visibility ~ WS.? ~ valueList)
      yield new Annotation(path, isVisible, values)
  }
}