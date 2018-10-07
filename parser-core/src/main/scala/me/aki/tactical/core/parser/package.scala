package me.aki.tactical.core

import fastparse.all._

package object parser {
  trait Parser[T] {
    def parser() : P[T]

    def parse(string: String): T = parser().parse(string).get.value
  }

  val boolean = P {
    val trueParser = "true".!.map(_ => true)
    val falseParser = "false".!.map(_ => false)

    trueParser | falseParser
  }

  private val digit = CharIn('0' to '9').rep(min = 1)
  val integerNumber = P { "-".? ~ digit } opaque "<number>"
  val floatingNumber = P {
    val num = ("." ~ digit) | (digit ~ ("." ~ digit.?).?)
    "-".? ~ num ~ (("e" | "E") ~ integerNumber).?
  } opaque "<floating-point number>"

  /** A parser that maps a string to a value */
  class StringParser[T](string: String, value: => T) extends Parser[T] {
    override def parser(): P[T] = P { string } map (_ => value)
  }

  implicit def parserToP[T](parser: Parser[T]): P[T] = parser.parser()
}
