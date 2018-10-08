package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._
import me.aki.tactical.core.typeannotation.TargetType
import me.aki.tactical.core.typeannotation.TargetType._
import me.aki.tactical.core.typeannotation.TypePath
import me.aki.tactical.core.typeannotation.TypePath.Kind

object TypePathKindParser extends Parser[Kind] {
  val parser: P[Kind] = P {
    val array = for (_ ← "[]".!) yield new Kind.Array()
    val inner = for (_ ← ".".!) yield new Kind.InnerClass()
    val wildcard = for (_ ← "?".!) yield new Kind.WildcardBound()
    val typeArgument = P[Kind] {
      for (i ← "<" ~ integerNumber.! ~ ">")
        yield new Kind.TypeArgument(i.toInt)
    }

    array | inner | wildcard | typeArgument
  }
}

object TypePathParser extends Parser[TypePath] {
  val parser: P[TypePath] = P {
    val kinds = TypePathKindParser.rep(min = 0, sep = WS.?)
    for (kinds ← "{" ~ WS.? ~ kinds ~ WS.? ~ "}") yield new TypePath(kinds.asJava)
  }
}

object TargetTypeParsers {
  import TargetType._
  val checkedException = for (i ← "exception" ~ WS ~ integerNumber.!) yield new CheckedException(i.toInt)
  val parameters = for (i ← "parameter" ~ WS ~ integerNumber.!) yield new MethodParameter(i.toInt)
  val receiver = for (_ ← "receiver".!) yield new MethodReceiver()
  val returnType = for (_ ← "return".!) yield new ReturnType()
  val typeParameter = for (i ← "type" ~ WS ~ "parameter" ~ WS ~ integerNumber.!) yield new TypeParameter(i.toInt)
  val typeParameterBound =
    for ((param, bound) ← "type" ~ WS ~ "parameter" ~ WS ~ "bound" ~ WS ~ integerNumber.! ~ WS ~ integerNumber.!)
      yield new TypeParameterBound(param.toInt, bound.toInt)
  val `extends` = for (_ ← "extends".!) yield new Extends()
  val `implementes` = for (i ← "implements" ~ WS ~ integerNumber.!) yield new Implements(i.toInt)
}

object MethodTargetTypeParser extends Parser[MethodTargetType] {
  val parser: P[MethodTargetType] = P {
    TargetTypeParsers.checkedException |
    TargetTypeParsers.parameters |
    TargetTypeParsers.receiver |
    TargetTypeParsers.returnType |
    TargetTypeParsers.typeParameter |
    TargetTypeParsers.typeParameterBound
  }
}

object ClassTargetTypeParser extends Parser[ClassTargetType] {
  val parser: P[ClassTargetType] = P {
    TargetTypeParsers.`extends` |
    TargetTypeParsers.`implementes` |
    TargetTypeParsers.typeParameter |
    TargetTypeParsers.typeParameterBound
  }
}

object InsnTargetTypeParser extends Parser[InsnTargetType] {
  val parser = ???
}

object LocalTargetTypeParser extends Parser[LocalTargetType] {
  val parser = ???
}
