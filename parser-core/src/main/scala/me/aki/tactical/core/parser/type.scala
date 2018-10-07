package me.aki.tactical.core.parser

import fastparse.all._
import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`._

object BooleanTypeParser extends StringParser[BooleanType]("boolean", BooleanType.getInstance())
object ByteTypeParser extends StringParser[ByteType]("byte", ByteType.getInstance())
object ShortTypeParser extends StringParser[ShortType]("short", ShortType.getInstance())
object CharTypeParser extends StringParser[CharType]("char", CharType.getInstance())
object IntTypeParser extends StringParser[IntType]("int", IntType.getInstance())
object LongTypeParser extends StringParser[LongType]("long", LongType.getInstance())
object FloatTypeParser extends StringParser[FloatType]("float", FloatType.getInstance())
object DoubleTypeParser extends StringParser[DoubleType]("double", DoubleType.getInstance())

object IntLikeTypeParser extends Parser[IntLikeType] {
  val parser: P[IntLikeType] = P {
    BooleanTypeParser | ByteTypeParser | ShortTypeParser | CharTypeParser | IntTypeParser
  }
}

object PrimitiveTypeParser extends Parser[PrimitiveType] {
  val parser: P[PrimitiveType] = {
    IntLikeTypeParser | LongTypeParser | FloatTypeParser | DoubleTypeParser
  }
}

object PathParser extends Parser[Path] {
  val parser: P[Path] =
    for (seq ← Literal.rep(min = 1, sep = WS.? ~ "." ~ WS.?))
      yield new Path(seq.init.toArray, seq.last)
}

object ObjectTypeParser extends Parser[ObjectType] {
  val parser: P[ObjectType] = P {
    for (path ← PathParser) yield new ObjectType(path)
  }
}

abstract class ArrayIncludingParser(baseTypeParser: P[Type]) extends Parser[Type] {
  val parser: P[Type] = P {
    baseTypeParser ~ P("[" ~ WS.? ~ "]").!.rep(min = 0, sep = WS.?) map {
      case (typ, Seq()) => typ
      case (baseType, dimensions) => new ArrayType(baseType, dimensions.length)
    }
  }
}

object RefTypeParser extends ArrayIncludingParser(ObjectTypeParser)

object TypeParser extends ArrayIncludingParser(PrimitiveTypeParser | ObjectTypeParser)
