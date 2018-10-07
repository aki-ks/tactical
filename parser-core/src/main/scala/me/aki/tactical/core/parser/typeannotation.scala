package me.aki.tactical.core.parser

import scala.collection.JavaConverters._
import fastparse.all._
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