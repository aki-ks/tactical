package me.aki.tactical.core

import fastparse.all._

import scala.util.Try

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
  private val integerNumber = P { "-".? ~ digit } opaque "<number>"
  private val floatingNumber = P {
    val num = ("." ~ digit) | (digit ~ ("." ~ digit.?).?)
    "-".? ~ num ~ (("e" | "E") ~ integerNumber).?
  } opaque "<floating-point number>"

  val byte: P[Byte] = for (byte ← integerNumber.! if Try(byte.toByte).isSuccess) yield byte.toByte
  val short: P[Short] = for (short ← integerNumber.! if Try(short.toShort).isSuccess) yield short.toShort
  val int: P[Int] = for (int ← integerNumber.! if Try(int.toInt).isSuccess) yield int.toInt
  val long: P[Long] = for (long ← integerNumber.! if Try(long.toLong).isSuccess) yield long.toLong
  val float: P[Float] = for (float ← floatingNumber.! if Try(float.toFloat).isSuccess) yield float.toFloat
  val double: P[Double] = for (double ← floatingNumber.! if Try(double.toDouble).isSuccess) yield double.toDouble

  /** A parser that maps a string to a value */
  class StringParser[T](string: String, value: => T) extends Parser[T] {
    override def parser(): P[T] = P { string } map (_ => value)
  }

  implicit def parserToP[T](parser: Parser[T]): P[T] = parser.parser()
}
