package me.aki.tactical.core.parser.test

import me.aki.tactical.core.constant._
import me.aki.tactical.core.parser.FieldConstantParser
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ConstantTest extends FlatSpec with Matchers with PropertyChecks {
  "The FieldConstantParser" should "parse int values" in {
    forAll { int: Int =>
      FieldConstantParser.parse(int.toString) shouldEqual new IntConstant(int)
    }
  }

  it should "parse long values" in {
    forAll { long: Long =>
      FieldConstantParser.parse(long + "l") shouldEqual new LongConstant(long)
      FieldConstantParser.parse(long + "L") shouldEqual new LongConstant(long)
    }
  }

  it should "parse float values" in {
    forAll { float: Float =>
      FieldConstantParser.parse(float + "f") shouldEqual new FloatConstant(float)
      FieldConstantParser.parse(float + "F") shouldEqual new FloatConstant(float)
    }
  }

  it should "parse double values" in {
    forAll { double: Double =>
      FieldConstantParser.parse(double + "d") shouldEqual new DoubleConstant(double)
      FieldConstantParser.parse(double + "D") shouldEqual new DoubleConstant(double)
    }
  }

  it should "parse string values" in {
    forAll { string: String =>
      val escapedString = string.replace("\"", "\\\"")
      FieldConstantParser.parse('"' + escapedString + '"') shouldEqual new StringConstant(string)
    }
  }


}
