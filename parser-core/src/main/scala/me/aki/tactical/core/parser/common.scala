package me.aki.tactical.core.parser

import fastparse.all._

/** Parse whitespace character(s) */
object WS extends Parser[Unit] {
  val parser: P[Unit] = P {
    CharIn(" \t\n\r").rep(min = 1)
  } opaque "<whitespace>"
}

/** Parse one character wrapped by apostrophes */
object CharLiteral extends Parser[Char] {
  val escapedChar: P[Char] = P {
    val hexDigit = P { CharIn('0' to '9', 'a' to 'f', 'A' to 'F') }.!

    "\\" ~ {
      "t".!.map(_ => '\t') |
        "b".!.map(_ => '\b') |
        "n".!.map(_ => '\n') |
        "r".!.map(_ => '\r') |
        "f".!.map(_ => '\f') |
        "\\".!.map(_ => '\\') |
        ("u" ~ (hexDigit ~ hexDigit ~ hexDigit ~ hexDigit).!).map {
          digits => Integer.parseInt(digits, 16).toChar
        }
    }
  }

  val parser: P[Char] = P {
    "'" ~ (escapedChar | AnyChar.!.map(_.head)) ~ "'"
  }
}

/** Parse any characters between two quotes */
object StringLiteral extends Parser[String] {
  val parser: P[String] = {
    val escapedQuote = P("\\\"").!.map(_ => "\"")
    "\"" ~ (CharLiteral.escapedChar | escapedQuote | CharPred(_ != '\"').!).rep(min = 0).map(_.mkString) ~ "\""
  }
}

/**
  * Parse a string where the first character is a letter and the other characters are letters, digits, '$' or '_'.
  *
  * Any string can be parsed if it is wrapped within a grave accents.
  */
object Literal extends Parser[String] {
  val regularString = P {
    val letter = CharIn('a' to 'z', 'A' to 'Z')

    val head = letter
    val tail = P { letter | CharIn('0' to '9', "_", "$") }.rep(min = 0)
    head ~ tail
  }.!

  val escapedString = P {
    val escapableChar = P {
      val escapedQuote = "\\`".!.map(_ => "`")

      CharLiteral.escapedChar | escapedQuote | CharPred(_ != '`').!
    }

    "`" ~/ escapableChar.rep(min = 0).map(_.mkString) ~/ "`"
  }

  override val parser: P[String] = escapedString | regularString
}
