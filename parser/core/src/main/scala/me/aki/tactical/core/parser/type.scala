package me.aki.tactical.core.parser

import scala.collection.JavaConverters._

import java.util.Optional

import fastparse.all._
import me.aki.tactical.core._
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
  val parser: P[Path] = P {
    for (seq ← Literal.rep(min = 1, sep = WS.? ~ "." ~ WS.?))
      yield new Path(seq.init.toArray, seq.last)
  } opaque "<class name>"
}

object ObjectTypeParser extends Parser[ObjectType] {
  val parser: P[ObjectType] = P {
    for (path ← PathParser) yield new ObjectType(path)
  }
}

class ArrayIncludingParser[BaseType >: ArrayType <: Type](baseTypeParser: P[BaseType], opaque: String = null) extends Parser[BaseType] {
  val parser: P[BaseType] = {
    val parser = P {
      baseTypeParser ~ P("[" ~ WS.? ~ "]").!.rep(min = 0, sep = WS.?) map {
        case (typ, Seq()) => typ
        case (baseType, dimensions) => new ArrayType(baseType, dimensions.length)
      }
    }

    if (opaque == null) parser
    else parser.opaque(opaque)
  }
}

object ArrayBaseTypeParser extends Parser[Type] {
  val parser: P[Type] = PrimitiveTypeParser | ObjectTypeParser
}

object RefTypeParser extends ArrayIncludingParser[RefType](ObjectTypeParser, "<object type | array type>")

object TypeParser extends ArrayIncludingParser[Type](ArrayBaseTypeParser, "<type>")

/** Parse <type>.class */
object ClassLiteral extends Parser[Type] {
  val parser: P[Type] = P {
    val typeParser = P {
      val objectTypeParser = P {
        (!"class" ~ Literal).rep(min = 1, sep = WS.? ~ "." ~ WS.?)
      } map {
        case seq => new Path(seq.init.toArray, seq.last)
      } map { path => new ObjectType(path) }

      val nonArray = BooleanTypeParser | ByteTypeParser | ShortTypeParser | CharTypeParser |
        IntTypeParser | LongTypeParser | FloatTypeParser | DoubleTypeParser | objectTypeParser

      new ArrayIncludingParser(nonArray)
    }

    typeParser ~ WS.? ~ "." ~ WS.? ~ "class"
  } opaque "<type>.class"
}

object ReturnTypeParser extends Parser[Optional[Type]] {
  val parser: P[Optional[Type]] = P {
    val voidType = for(_ ← "void".!) yield Optional.empty[Type]
    val otherType = for (typ ← TypeParser) yield Optional.of(typ)
    voidType | otherType
  }
}

object MethodDescriptorParser extends Parser[MethodDescriptor] {
  val parser: P[MethodDescriptor] =
    for ((paramTypes, returnType) ← "(" ~ WS.? ~ TypeParser.rep(sep = WS.? ~ "," ~ WS.?) ~ WS.? ~ ")" ~ WS.? ~ ReturnTypeParser)
      yield new MethodDescriptor(paramTypes.asJava, returnType)
}

/**
  * Textify a type as used as parameter in instructions
  */
object InsnTypeParser {
  val b = ByteTypeParser
  val s = ShortTypeParser
  val c = CharTypeParser
  val i = IntTypeParser
  val l = LongTypeParser
  val f = FloatTypeParser
  val d = DoubleTypeParser
  val ref = P { "ref".!.map(_ => ObjectType.OBJECT) }

  val fd = P { f | d }
  val il = P { i | l }
  val ilfd = P { i | l | f | d }
  val ilfdref = P { i | l | f | d | ref }
  val bscilfd = P { b | s | c | i | l | f | d }
}
