package me.aki.tactical.core.parser.test

import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`._
import me.aki.tactical.core.parser.TypeParser
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.PropertyChecks

class TypeParserTest extends FlatSpec with Matchers with PropertyChecks {
  "TypeParser" should "parse all primitive types" in {
    TypeParser.parse("boolean") shouldEqual BooleanType.getInstance()
    TypeParser.parse("byte") shouldEqual ByteType.getInstance()
    TypeParser.parse("short") shouldEqual ShortType.getInstance()
    TypeParser.parse("char") shouldEqual CharType.getInstance()
    TypeParser.parse("int") shouldEqual IntType.getInstance()
    TypeParser.parse("long") shouldEqual LongType.getInstance()
    TypeParser.parse("float") shouldEqual FloatType.getInstance()
    TypeParser.parse("double") shouldEqual DoubleType.getInstance()
  }

  it should "parse object types" in {
    TypeParser.parse("java.lang.String") shouldEqual new ObjectType(Path.of("java", "lang", "String"))
  }

  it should "parse array types" in {
    TypeParser.parse("int[]") shouldEqual new ArrayType(IntType.getInstance(), 1)
    TypeParser.parse("java.lang.String[]") shouldEqual new ArrayType(new ObjectType(Path.of("java", "lang", "String")), 1)
    TypeParser.parse("java.lang.String[][][]") shouldEqual new ArrayType(new ObjectType(Path.of("java", "lang", "String")), 3)
  }
}
