package me.aki.tactical.core.parser.test

import me.aki.tactical.core.parser.{CharLiteral, Literal, StringLiteral}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class CommonTest extends FlatSpec with Matchers with PropertyChecks {
  "The CharLiteral parser " should "parse any character" in {
    forAll { char: Char =>
      CharLiteral.parse(s"'$char'") shouldEqual char
    }
  }

  it should "parse escaped characters" in {
    CharLiteral.parse("'\\t'") shouldEqual '\t'
    CharLiteral.parse("'\\b'") shouldEqual '\b'
    CharLiteral.parse("'\\r'") shouldEqual '\r'
    CharLiteral.parse("'\\n'") shouldEqual '\n'
    CharLiteral.parse("'\\f'") shouldEqual '\f'
    CharLiteral.parse("'\\\\'") shouldEqual '\\'
  }

  it should "parse unicode escapes" in {
    CharLiteral.parse("'\\u0FF0'") shouldEqual '\u0ff0'
    CharLiteral.parse("'\\u0ff0'") shouldEqual '\u0ff0'
    CharLiteral.parse("'\\u4321'") shouldEqual '\u4321'
  }

  "The Literal parser" should "parse unescaped strings" in {
    Literal.parse("abc") shouldEqual "abc"
    Literal.parse("abc123$_") shouldEqual "abc123$_"
  }

  it should "parse escaped Strings with any content" in {
    Literal.parse("`abc123äöüß`") shouldEqual "abc123äöüß"
    Literal.parse("`\\``") shouldEqual "`"
    Literal.parse("``") shouldEqual ""

    forAll { string: String =>
      Literal.parse(s"`$string`") shouldEqual string
    }
  }

  "The StringLiteral parser" should "parse strings with any content" in {
    StringLiteral.parse('"' + "\"" + '"') shouldEqual ""
    StringLiteral.parse('"' + "\\\""+ '"') shouldEqual "\""
    StringLiteral.parse('"' + "\\t\\n" + '"') shouldEqual "\t\n"

    forAll { string: String =>
      StringLiteral.parse('"' + string + '"') shouldEqual string
    }
  }
}
