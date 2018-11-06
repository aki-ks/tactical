package me.aki.tactical.core.parser.test

import me.aki.tactical.core.Attribute
import me.aki.tactical.core.parser.AttributeParser
import me.aki.tactical.core.textify.AttributeTextifier
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class AttributeTest extends FlatSpec with Matchers with PropertyChecks {
  "The AttributeParser" should "parse attributes with arbitrary names" in {
    forAll { name: String =>
      val escapedName = name.replace("\"", "\\\"")

      AttributeParser.parse("attribute \"" + escapedName + "\" {}") shouldEqual new Attribute(name, Array())
    }
  }

  it should "parse any attributes with any value" in {
    AttributeParser.parse("attribute \"foo\" { 00 }") shouldEqual new Attribute("foo", Array(0))
    AttributeParser.parse("attribute \"foo\" { ff }") shouldEqual new Attribute("foo", Array(-1))
    AttributeParser.parse("attribute \"foo\" { 10 ff }") shouldEqual new Attribute("foo", Array(16, -1))

    forAll { data: Array[Byte] =>
      def toHexByte(byte: Byte) = (256 | byte).toHexString.takeRight(2)
      val hexData = data.map(toHexByte).mkString(" ")

      AttributeParser.parse("attribute \"foo\" { " + hexData + " }") shouldEqual new Attribute("foo", data)
    }
  }

  it should "parse random textified attributes" in {
    generatorTest(CoreGenerator.attribute, AttributeParser, AttributeTextifier.getInstance())
  }
}
