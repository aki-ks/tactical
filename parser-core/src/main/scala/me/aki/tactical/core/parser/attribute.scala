package me.aki.tactical.core.parser

import fastparse.all._
import me.aki.tactical.core.Attribute

object AttributeParser extends Parser[Attribute] {
  val parser: P[Attribute] = P {
    val hexDigit = CharIn('0' to '9', 'a' to 'f', 'A' to 'F')
    val hexData = P { hexDigit ~ hexDigit }.!.rep(min = 0, sep = WS.?)

    for((name, data) ← "attribute" ~ WS.? ~ StringLiteral ~ WS.? ~ "{" ~ WS.? ~ hexData ~ WS.? ~ "}")
      yield new Attribute(name, data.map(Integer.parseInt(_, 16).toByte).toArray)
  }
}
