package me.aki.tactical.core.parser.test

import me.aki.tactical.core.Path
import me.aki.tactical.core.`type`.{ArrayType, IntType, ObjectType}

import scala.collection.JavaConverters._
import me.aki.tactical.core.annotation._
import me.aki.tactical.core.parser.AnnotationValueParser
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class AnnotationParserTest extends FlatSpec with Matchers with PropertyChecks {
  "The AnnotationValueParser" should "parse primitive values" in {
    forAll { boolean: Boolean =>
      AnnotationValueParser.parse(boolean.toString) shouldEqual new BooleanAnnotationValue(boolean)
    }

    forAll { byte: Byte =>
      AnnotationValueParser.parse(s"${byte}b") shouldEqual new ByteAnnotationValue(byte)
      AnnotationValueParser.parse(s"${byte}B") shouldEqual new ByteAnnotationValue(byte)
    }

    forAll { short: Short =>
      AnnotationValueParser.parse(s"${short}s") shouldEqual new ShortAnnotationValue(short)
      AnnotationValueParser.parse(s"${short}S") shouldEqual new ShortAnnotationValue(short)
    }

    forAll { char: Char =>
      AnnotationValueParser.parse(s"'$char'") shouldEqual new CharAnnotationValue(char)
    }

    forAll { int: Int =>
      AnnotationValueParser.parse(s"$int") shouldEqual new IntAnnotationValue(int)
    }

    forAll { long: Long =>
      AnnotationValueParser.parse(s"${long}l") shouldEqual new LongAnnotationValue(long)
      AnnotationValueParser.parse(s"${long}L") shouldEqual new LongAnnotationValue(long)
    }

    forAll { float: Float =>
      AnnotationValueParser.parse(s"${float}f") shouldEqual new FloatAnnotationValue(float)
      AnnotationValueParser.parse(s"${float}F") shouldEqual new FloatAnnotationValue(float)
    }

    forAll { double: Double =>
      AnnotationValueParser.parse(s"${double}d") shouldEqual new DoubleAnnotationValue(double)
      AnnotationValueParser.parse(s"${double}D") shouldEqual new DoubleAnnotationValue(double)
    }
  }

  it should "parse string values" in {
    forAll { string: String =>
      val escapedString = string.replace("\"", "\\\"")
      AnnotationValueParser.parse('"' + escapedString + '"') shouldEqual new StringAnnotationValue(string)
    }
  }

  it should "parse arrays" in {
    def arrayValue(ints: Int*) =
      new ArrayAnnotationValue(ints.map(int => new IntAnnotationValue(int): AnnotationValue).asJava)

    AnnotationValueParser.parse("{}") shouldEqual arrayValue()
    AnnotationValueParser.parse("{1,2,3}") shouldEqual arrayValue(1, 2, 3)
    AnnotationValueParser.parse("{ 1 , 2 , 3 }") shouldEqual arrayValue(1, 2, 3)
  }

  it should "parse class types" in {
    AnnotationValueParser.parse("int.class") shouldEqual new ClassAnnotationValue(IntType.getInstance())
    AnnotationValueParser.parse("int[][].class") shouldEqual new ClassAnnotationValue(new ArrayType(IntType.getInstance(), 2))
    AnnotationValueParser.parse("java.lang.String.class") shouldEqual new ClassAnnotationValue(ObjectType.STRING)
  }

  it should "parse enum types" in {
    AnnotationValueParser.parse("EnumClass.ENUM_VALUE") shouldEqual new EnumAnnotationValue(Path.of("EnumClass"), "ENUM_VALUE")
    AnnotationValueParser.parse("com.example.EnumClass.ENUM_VALUE") shouldEqual new EnumAnnotationValue(Path.of("com", "example", "EnumClass"), "ENUM_VALUE")
  }

  it should "parse annotation annotation values" in {
    AnnotationValueParser.parse("@java.lang.Override()") shouldEqual
      new AnnotationAnnotationValue(Path.of("java", "lang", "Override"))

    val values = new java.util.LinkedHashMap[String, AnnotationValue](Map(
      "value" -> new IntAnnotationValue(10),
      "clazz" -> new ClassAnnotationValue(ObjectType.STRING)
    ).asJava)
    AnnotationValueParser.parse("@java.lang.Override(value = 10, clazz = java.lang.String.class)") shouldEqual
      new AnnotationAnnotationValue(Path.of("java", "lang", "Override"), values)
  }

  "The AnnotationParser" should "parse regualar annotations" in {

  }
}