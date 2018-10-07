package me.aki.tactical.core

import fastparse.all._

package object parser {
  trait Parser[T] {
    def parser() : P[T]

    def parse(string: String): T = parser().parse(string).get.value
  }

  /** A parser that maps a string to a value */
  class StringParser[T](string: String, value: => T) extends Parser[T] {
    override def parser(): P[T] = P { string } map (_ => value)
  }

  implicit def parserToP[T](parser: Parser[T]): P[T] = parser.parser()
}
